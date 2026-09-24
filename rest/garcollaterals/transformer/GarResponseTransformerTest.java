package com.ing.bankguarantees.remote.rest.garcollaterals.transformer;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GarResponseTransformerTest {

    private GarResponseTransformer transformer;

    public static final String IDENTIFIER_CLIENT = "CLIENT-999";
    Identifier identifier = new Identifier().builder().type(IDENTIFIER_CLIENT).build();

    @BeforeEach
    void setUp() {
        transformer = new GarResponseTransformer();
    }

    @Test
    void shouldTransformResponseWithOneCollateralAndOneCovenant() {
        // Identification
        GarResponse.GarResponseDetails.Company.Identification identification =
                new GarResponse.GarResponseDetails.Company.Identification("ID123", "Test Company");

        // Collateral
        GarResponse.GarResponseDetails.Company.CollateralValue.Currency currency =
                new GarResponse.GarResponseDetails.Company.CollateralValue.Currency("EUR");

        GarResponse.GarResponseDetails.Company.CollateralValue collateralValue =
                new GarResponse.GarResponseDetails.Company.CollateralValue(BigDecimal.valueOf(1000), currency);

        Identifier involvedPartyId = new Identifier("LEI", "ID123");

        GarResponse.GarResponseDetails.Company.RegistrationDate regDate =
                new GarResponse.GarResponseDetails.Company.RegistrationDate("20250606");

        GarResponse.GarResponseDetails.Company.CollateralResponse collateral =
                GarResponse.GarResponseDetails.Company.CollateralResponse.builder()
                        .collateralType("Deposit")
                        .collateralValue(collateralValue)
                        .involvedPartyIdentifier(involvedPartyId)
                        .assetDescription("Some Asset")
                        .comments("No comment")
                        .registrationDate(regDate)
                        .build();

        // Covenant
        GarResponse.GarResponseDetails.Company.CovenantResponse covenant =
                new GarResponse.GarResponseDetails.Company.CovenantResponse();
        covenant.setCollateralType("Guarantee");
        covenant.setCollateralValue(collateralValue);
        covenant.setInvolvedPartyIdentifier(involvedPartyId);
        covenant.setComments("Covenant test");
        covenant.setRegistrationDate(regDate);

        // Company
        GarResponse.GarResponseDetails.Company company = new GarResponse.GarResponseDetails.Company();
        company.setIdentification(List.of(identification));
        company.setCollaterals(List.of(collateral));
        company.setCovenants(List.of(covenant));

        // Response
        GarResponse.GarResponseDetails details = new GarResponse.GarResponseDetails();
        details.setCompany(company);

        GarResponse garResponse = new GarResponse();
        garResponse.setGarResponse(List.of(details));

        GarCollateralsInput input = new GarCollateralsInput();
        input.setIdentifier(identifier);

        // When
        Optional<GarOutput> output = transformer.transform(garResponse, input);

        // Then
        assertTrue(output.isPresent());
        assertEquals(1, output.get().getCollaterals().size());
        assertEquals(1, output.get().getCovenants().size());

        GarOutput.Collateral transformedCollateral = output.get().getCollaterals().get(0);
        assertEquals("Deposit", transformedCollateral.getCollateralType());
        assertEquals("EUR", transformedCollateral.getGarAmount().getCurrency());
        assertEquals(new BigDecimal("1000"), transformedCollateral.getGarAmount().getValue());
    }

    @Test
    void shouldReturnEmptyWhenGarResponseListIsNull() {
        GarResponse garResponse = new GarResponse();
        garResponse.setGarResponse(null);

        GarCollateralsInput input = new GarCollateralsInput();
        input.setIdentifier(identifier);

        Optional<GarOutput> output = transformer.transform(garResponse, input);
        assertTrue(output.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenGarResponseListIsEmpty() {
        GarResponse garResponse = new GarResponse();
        garResponse.setGarResponse(Collections.emptyList());

        GarCollateralsInput input = new GarCollateralsInput();
        input.setIdentifier(identifier);

        Optional<GarOutput> output = transformer.transform(garResponse, input);
        assertTrue(output.isEmpty());
    }
}
