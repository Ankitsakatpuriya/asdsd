package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.models.enums.BeneficiaryType;
import com.ing.bankguarantees.validation.annotation.ValidCbeNumber;
import com.ing.bankguarantees.validation.annotation.ValidEmailAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryPayload {

    @ValidCbeNumber
    private String cinNumber;

    @NotNull
    private BeneficiaryType beneficiaryType;

    private BeneficiaryOrganisationNamePayload organisationName;
    private BeneficiaryPrivateIndividualPayload privateIndividual;
    private BeneficiaryPostalAddressPayload postalAddress;

    @Valid
    private BeneficiaryEmailPayload emailAddress;

    @Data
    @Valid
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryPostalAddressPayload {

        @NotBlank
        private String countryCode;

        @NotBlank
        private String cityName;

        @NotBlank
        private String postalCode;

        @NotBlank
        private String firstAddress;

        private String streetName;

        private String houseNumber;
    }

    @Data
    @Valid
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryOrganisationNamePayload {

        @NotBlank
        private String fullName;
    }

    @Data
    @Valid
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryPrivateIndividualPayload {

        private boolean belgiumCitizen;
        @NotBlank
        private String primaryBeneficiaryName;
        private String primaryIdentificationReference;
        private LocalDate primaryBeneficiaryDob;


        private String secondaryBeneficiaryName;
        private String secondaryIdentificationReference;
        private LocalDate secondaryBeneficiaryDob;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BeneficiaryEmailPayload {

        @ValidEmailAddress
        public String emailIdInformation;

    }
}
