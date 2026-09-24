package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantData {

    private String cinNumber;
    private ApplicantOrganisationNameData organisationName;
    private ApplicantPostalAddressData postalAddress;
    private ApplicantEmailData emailAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantPostalAddressData {

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
    public static class ApplicantOrganisationNameData {

        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantEmailData {

        private String emailIdInformation;

    }
}
