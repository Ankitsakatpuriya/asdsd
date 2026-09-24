package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.validation.annotation.ValidCbeNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantPayload {

    @ValidCbeNumber
    private String cinNumber;
    private ApplicantOrganisationNamePayload organisationName;
    private ApplicantPostalAddressPayload postalAddress;


    @Data
    @Valid
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantPostalAddressPayload {

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
    public static class ApplicantOrganisationNamePayload {

        @NotBlank
        private String fullName;
    }
}
