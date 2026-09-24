package com.ing.bankguarantees.remote.rest.productagreement.transformer;

import com.ing.bankguarantees.remote.rest.productagreement.ProductAgreementProperties;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse.ProductAgreementDetailsResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@Slf4j
@ExtendWith(MockitoExtension.class)
class ProductAgreRespTransformerTest {
    private static final String ORG_ID = "0325895254";

    @Mock
    ProductAgreementProperties properties;


    @Test
    void transformTest() {

        given(properties.getProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC", "BE_ING_MGN_CRN_AC", "BE_ING_CORP_AC", "BE_ING_GNT_GVN"));
        given(properties.getProductStatus()).willReturn("EFF_AR");
        ProductAgreementResponse productAgreements = MockHelper.createProductAgreements("BGA/FDA/ProductAgreements.json");
        List<ProductAgreementAccount> listProductAgreementAccount = new ProductAgreeRespTransformer(properties).transform(productAgreements, ORG_ID);
        assertThat(listProductAgreementAccount).isNotEmpty();
        List<String> actualIdList = listProductAgreementAccount.stream().map(ProductAgreementAccount::getUuid).toList();
        List<String> expectedIdList = List.of("885d30f0-b290-45a7-a7db-f4755e6b6af4", "302802c4-e74f-4f09-87f0-0b54da275592", "302802c4-e74f-4f09-87f0-0b54da275589", "302802c4-e74f-4f09-87f0-0b54da275591");
        assertThat(actualIdList).isEqualTo(expectedIdList);

    }

    @Test
    void transformElsePartTest() {

        given(properties.getProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC", "BE_ING_MGN_CRN_AC", "BE_ING_CORP_AC", "BE_ING_GNT_GVN"));
        given(properties.getProductStatus()).willReturn("EFF_AR");
        ProductAgreementResponse productAgreements = MockHelper.createProductAgreements("BGA/FDA/ProductAgreementsErrorData.json");
        List<ProductAgreementAccount> listProductAgreementAccount = new ProductAgreeRespTransformer(properties).transform(productAgreements, ORG_ID);
        assertThat(listProductAgreementAccount).isNotEmpty();
        List<String> actualIdList = listProductAgreementAccount.stream().map(ProductAgreementAccount::getUuid).toList();
        List<String> expectedIdList = List.of("885d30f0-b290-45a7-a7db-f4755e6b6af4", "302802c4-e74f-4f09-87f0-0b54da275592", "302802c4-e74f-4f09-87f0-0b54da275591");
        assertThat(actualIdList).isEqualTo(expectedIdList);

    }

    @Test
    void transformEmptyTest() {

        ProductAgreementResponse productAgreementResponse = ProductAgreementResponse.builder().productAgreements(new ProductAgreementResponse.ProductAgreementsResponse()).build();
        List<ProductAgreementAccount> listProductAgreementAccount = new ProductAgreeRespTransformer(properties).transform(productAgreementResponse, ORG_ID);
        assertThat(listProductAgreementAccount).isEmpty();

    }


    @Test
    void transformProductTypeNullTest() {

        ProductAgreementResponse productAgreements = MockHelper.createProductAgreements("BGA/FDA/ProductAgreements.json");
        ProductAgreementResponse productTypeNullProductAgreement = getNullProductTypeList(productAgreements);
        List<ProductAgreementAccount> listProductAgreementAccount = new ProductAgreeRespTransformer(properties).transform(productTypeNullProductAgreement, ORG_ID);
        assertThat(listProductAgreementAccount).isEmpty();

    }


    private ProductAgreementResponse getNullProductTypeList(ProductAgreementResponse productAgreement) {
        List<ProductAgreementDetailsResponse> list = productAgreement.getProductAgreements().getData()
                .stream()
                .peek(result -> result.setProductType(null))
                .toList();
        productAgreement.getProductAgreements().setData(list);
        return productAgreement;
    }
}

