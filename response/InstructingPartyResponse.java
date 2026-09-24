
package com.ing.bankguarantees.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructingPartyResponse {

    private IndividualResponse individual;
    private OrganisationResponse organisation;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationResponse  {

        private String legalEntityId;

        private String cinNumber;
        private OrganisationNameResponse organisationName;
        private PostalAddressResponse postalAddress;
        private List<DigitalAddressResponse> digitalAddresses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualResponse  {

        private String legalRepId;
        private IndividualName individualName;
        private List<DigitalAddressResponse> digitalAddresses;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualName {
        private String firstName;

        private String lastName;

        private String fullName;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostalAddressResponse {

        private String countryCode;

        private String cityName;

        private String postalCode;

        private String firstAddress;

        private String streetName;

        private String houseNumber;

        private String houseNumberAddition;

        private String usageType;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationNameResponse {

        private String type;

        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DigitalAddressResponse {

        private String digitalAddressType;
        private String digitalAddressUsageType;
        private String fullDigitalAddress;
    }
}

