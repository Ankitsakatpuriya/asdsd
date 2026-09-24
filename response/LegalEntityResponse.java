package com.ing.bankguarantees.models.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalEntityResponse {

    private String cinNumber;
    private LegalEntityOrganisationNameResponse organisationName;
    private LegalEntityPostalAddressResponse postalAddress;
    private LegalEntityEmail emailAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegalEntityPostalAddressResponse{

        @NotBlank
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
    public static class LegalEntityOrganisationNameResponse {

        private String type;

        private String fullName;
    }

    @Data
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegalEntityEmail {

        public String emailIdInformation;

    }
}
