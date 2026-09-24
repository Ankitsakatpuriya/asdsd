package com.ing.bankguarantees.remote.rest.garcollaterals.transformer;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput.Collateral;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput.Collateral.GarParty;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput.Covenant;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput.GarAmount;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarResponse;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarResponse.GarResponseDetails.Company.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;


@Slf4j
@Component
public class GarResponseTransformer implements InputTransformer<GarResponse, GarCollateralsInput, Optional<GarOutput>> {

    private static final String DEFAULT_DATE_VALUE = "0";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Override
    public Optional<GarOutput> transform( GarResponse response,  GarCollateralsInput garCollateralsIn) {
        log.info("GarResponseTransformer [transform] Response receive for Gar API ");

        if (CollectionUtils.isEmpty(response.getGarResponse())) {
            log.info("Empty response received from GarCollateral API for the legal entity identifier {} ", garCollateralsIn.getIdentifier());
            return Optional.empty();
        }


         GarOutput garOutput = new GarOutput();
         List<Collateral> collaterals = new ArrayList<>();
         List<Covenant> covenants = new ArrayList<>();


        response.getGarResponse()
                .forEach(garResp -> {
                     Map<String, String> companyIdentifierNameMap =
                            mapNames(ofNullable(garResp.getCompany())
                                    .flatMap(x -> ofNullable(x.getIdentification()))
                                    .orElse(Collections.emptyList()));
                    if (garResp.getCompany() != null) {
                        collaterals.addAll(transformCollaterals(companyIdentifierNameMap,
                                garResp.getCompany().getCollaterals()));
                        covenants.addAll(transformCovenants(companyIdentifierNameMap,
                                garResp.getCompany().getCovenants()));
                    }
                });
        garOutput.setCollaterals(collaterals);
        garOutput.setCovenants(covenants);

        return Optional.of(garOutput);
    }

    private List<Collateral> transformCollaterals( Map<String, String> mapNames,
                                                   @NotNull List<CollateralResponse> collaterals) {
        return collaterals.stream().map(collateral -> Collateral.builder()
                        .collateralType(collateral.getCollateralType())
                        .date(getRegisterDate(collateral.getRegistrationDate()))
                        .garAmount(GarAmount.builder()
                                .currency(getCurrencyCode(collateral.getCollateralValue()))
                                .value(getAmountValue(collateral.getCollateralValue()))
                                .build())
                        .grantedBy(transformToGarParties(mapNames, getValue(collateral.getInvolvedPartyIdentifier()),
                                getType(collateral.getInvolvedPartyIdentifier())))
                        .generalCoverType(collateral.getGeneralCoverType())
                        .assertDescription(collateral.getAssetDescription())
                        .comments(collateral.getComments())
                        .build())
                .toList();

    }

    private List<GarParty> transformToGarParties( Map<String, String> mapNames,  String value,  String type) {
        if (Objects.isNull(value)) {
            return Collections.emptyList();
        }
        return Stream.of(value.split("-"))
                .map(val -> getNameForOrganisation(mapNames, val))
                .filter(Objects::nonNull)
                .map(val -> GarParty.builder()
                        .name(mapNames.get(val))
                        .identifier(new Identifier(type, val))
                        .build())
                .toList();
    }

    private String getNameForOrganisation( Map<String, String> mapNames,  String val) {
        if (mapNames.containsKey(val)) {
            return val;
        }
        log.warn("Name not found for identifier {}", val);
        return null;
    }


    private List<Covenant> transformCovenants( Map<String, String> mapNames,  @NotNull List<CovenantResponse> covenants) {
        return covenants.stream().map(covenant -> Covenant.builder()
                        .agreementType(covenant.getCollateralType())
                        .date(getRegisterDate(covenant.getRegistrationDate()))
                        .garAmount(GarAmount.builder()
                                .currency(getCurrencyCode(covenant.getCollateralValue()))
                                .value(getAmountValue(covenant.getCollateralValue()))
                                .build())
                        .grantedBy(transformToGarParties(mapNames, getValue(covenant.getInvolvedPartyIdentifier()),
                                getType(covenant.getInvolvedPartyIdentifier())))
                        .generalCoverType(covenant.getGeneralCoverType())
                        .comments(covenant.getComments())
                        .build())
                .toList();
    }

    private String getCurrencyCode( CollateralValue collateralValue) {
        return ofNullable(collateralValue)
                .flatMap(x -> ofNullable(x.getCurrency()))
                .map(CollateralValue.Currency::getCode).orElse(null);
    }

    private BigDecimal getAmountValue( CollateralValue collateralValue) {
        return ofNullable(collateralValue)
                .flatMap(x -> ofNullable(x.getValue()))
                .orElse(null);
    }

    private String getValue( Identifier identifier) {
        return ofNullable(identifier)
                .flatMap(x -> ofNullable(x.getValue())).orElse(null);
    }

    private String getType( Identifier identifier) {
        return ofNullable(identifier)
                .flatMap(x -> ofNullable(x.getType())).orElse(null);
    }

    private Map<String, String> mapNames( List<Identification> identifications) {
        return identifications.stream()
                .collect(Collectors.toMap(Identification::getIdentifier, Identification::getName));
    }

    private LocalDate getRegisterDate(RegistrationDate registrationDate) {
        return ofNullable(registrationDate)
                .flatMap(x -> ofNullable(x.getCoverRegistrationDate()))
                .filter(x -> !x.equals(DEFAULT_DATE_VALUE))
                .map(x -> LocalDate.parse(x, formatter))
                .orElse(null);
    }
}
