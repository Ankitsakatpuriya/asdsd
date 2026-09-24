package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.BeneficiaryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryData {

    private String cinNumber;
    private BeneficiaryType beneficiaryType;
    private BeneficiaryOrganisationNameData organisationName;
    private BeneficiaryPrivateIndividualData privateIndividual;
    private BeneficiaryPostalAddressData postalAddress;
    private BeneficiaryEmailData emailAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryPostalAddressData {

        private String countryCode;

        private String cityName;

        private String postalCode;

        private String firstAddress;

        private String streetName;

        private String houseNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryOrganisationNameData {

        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryEmailData {

        private String emailIdInformation;

    }

    @Data
    @Valid
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryPrivateIndividualData {

        private boolean belgiumCitizen;
        @NotBlank
        private String primaryBeneficiaryName;
        private String primaryIdentificationReference;
        private LocalDate primaryBeneficiaryDob;

        private String secondaryBeneficiaryName;
        private String secondaryIdentificationReference;
        private LocalDate secondaryBeneficiaryDob;
    }
}