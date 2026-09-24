package com.ing.bankguarantees.remote.rest.productagreement.transformer;

import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.ProductAgreementProperties;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse.ProductAgreementDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Transformer for Accounts Product Agreements response
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAgreeRespTransformer implements InputTransformer<ProductAgreementResponse, String, List<ProductAgreementAccount>> {

    private final ProductAgreementProperties properties;
    private static final String IBAN = "IBAN";
    private static final String KLC = "BE_KLC";
    private static final String UUID = "UUID";
    private static final String BE_PAN = "BE_PAN";

    @Override
    public List<ProductAgreementAccount> transform(ProductAgreementResponse productAgreementResponse,
                                                   String organisationId) {
        log.info("ProductAgreRespTransformer [transform ] call for the OrgId {}", organisationId);
        return CollectionUtils.isEmpty(productAgreementResponse.getProductAgreements().getData()) ? Collections.emptyList()
                : mapAccounts(productAgreementResponse.getProductAgreements().getData(), organisationId);
    }

    private List<ProductAgreementAccount> mapAccounts(List<ProductAgreementDetailsResponse> productAgreementDetailsResponseList,
                                                      String organisationId) {
        log.info("ProductAgreRespTransformer [mapAccounts ] call for the OrgId {}", organisationId);
        Map<String, ProductAgreementAccount> productAgreements = productAgreementDetailsResponseList.stream()
                .filter(pad -> pad.getProductType() != null)
                .filter(pad -> properties.getProductTypes().contains(pad.getProductType()))
                .filter(pad -> properties.getProductStatus().equalsIgnoreCase(pad.getLifeCycleStatusType()))
                .filter(ProductAgreementDetailsResponse::isActive)
                .filter(pad -> containsIdentifiers(pad, organisationId))
                .map(pad -> {
                    Map<String, String> requiredIdentifiers = getRequiredIdentifiers(pad.getIdentifiers());
                    return ProductAgreementAccount.builder()
                            .accountName(pad.getName())
                            .iban(requiredIdentifiers.get(IBAN))
                            .creditLineAccountNumber(requiredIdentifiers.get(KLC))
                            .uuid(requiredIdentifiers.get(UUID))
                            .productType(pad.getProductType())
                            .currency(pad.getCurrency())
                            .bePan(requiredIdentifiers.get(BE_PAN))
                            .build();
                })
                .collect(Collectors.toMap(ProductAgreementAccount::getUuid, Function.identity()));

        logFilteredProductAgreements(productAgreementDetailsResponseList, productAgreements);
        return new ArrayList<>(productAgreements.values());
    }

    private boolean containsIdentifiers(ProductAgreementDetailsResponse productAgreementDetailsResponse,
                                        String organisationId) {
        Map<String, String> identifiers = getRequiredIdentifiers(productAgreementDetailsResponse.getIdentifiers());
        if ((identifiers.containsKey(IBAN) || identifiers.containsKey(KLC)) && identifiers.containsKey(UUID)) {
            return true;
        }
        log.info("Missing mandatory product agreement identifiers[IBAN/BE_KLC/UUID] for the OrgId {}", organisationId);
        return false;
    }

    private Map<String, String> getRequiredIdentifiers(List<ProductAgreementResponse.IdentifierResponse> identifierResponseList) {
        return identifierResponseList
                .stream()
                .filter(id -> IBAN.equalsIgnoreCase(id.getType()) || UUID.equalsIgnoreCase(id.getType())
                        || KLC.equalsIgnoreCase(id.getType()) || BE_PAN.equalsIgnoreCase(id.getType()))
                .collect(Collectors.toMap(ProductAgreementResponse.IdentifierResponse::getType,
                        ProductAgreementResponse.IdentifierResponse::getValue));
    }

    private void logFilteredProductAgreements(
            List<ProductAgreementDetailsResponse> productAgreementDetailsResponseList,
            Map<String, ProductAgreementAccount> productAgreementAccountMap) {
        productAgreementDetailsResponseList
                .forEach(pad -> {
                    String uuid = getRequiredIdentifiers(pad.getIdentifiers()).get(UUID);
                    if (!productAgreementAccountMap.containsKey(uuid)) {
                        log.info("Product agreement with uuid {}, type {}, status {}, effectiveDate {}, endDate {} is filtered out",
                                uuid, pad.getProductType(), pad.getLifeCycleStatusType(), pad.getEffectiveDate(), pad.getEndDate());
                    }
                });
    }
}
