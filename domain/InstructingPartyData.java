
package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class InstructingPartyData {

    private IndividualData individual;
    private OrganisationData organisation;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationData {

        private String legalEntityId;
        private String cinNumber;
        private String legalForm;
        private OrganisationNameData organisationName;
        private PostalAddressData postalAddress;
        private List<DigitalAddressData> digitalAddresses;
        private List<Identifier> internalIdentifiers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Validated
    public static class IndividualData {

        private String legalRepId;
        private IndividualNameData individualName;
        private List<DigitalAddressData> digitalAddresses;
        private List<Identifier> internalIdentifiers;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualNameData {
        private String fullName;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostalAddressData {

        private String countryCode;

        private String cityName;

        private String postalCode;

        private String firstAddress;

        private String streetName;

        private String houseNumber;

        private String usageType;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationNameData {

        private String type;

        private String fullName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DigitalAddressData {

        private String digitalAddressType;
        private String digitalAddressUsageType;
        private String fullDigitalAddress;
    }
}

