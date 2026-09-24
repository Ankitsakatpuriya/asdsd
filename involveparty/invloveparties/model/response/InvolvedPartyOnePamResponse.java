package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response from InvolvedParty API
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvolvedPartyOnePamResponse {

    private static final String OFFSET_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    private IndividualOnePamResponse individual;

    @Valid
    private InvolvedPartyOnePamResponse.OrganisationOnePamResponse organisation;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndividualOnePamResponse {
        private List<IdentifierOnePamResponse> involvedPartyInternalIdentifiers;
        private IndividualNameOnePamResponse individualName;
        private List<PostalAddressOnePamResponse> postalAddresses;
        private List<DigitalAddressOnePamResponse> digitalAddresses;
        private List<OrganisationOnePamResponse.GroupResponse> involvedPartyGroups;
        private String countryOfResidence;
        private String preferredLanguage;
        private String maritalStatus;
        private LocalDate effectiveDate;
        private String financialLegalStatusType;
        private String cityOfBirth;
        private String countryOfBirth;
        private LocalDate dateOfBirth;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class IndividualNameOnePamResponse {
            private String lastName;
            private String firstName1;
            private String nameInitials;
            private String salutation;
        }

    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationOnePamResponse {

        @NotNull
        @Valid
        private List<OrganisationNameOnePamResponse> organisationNames;

        private List<IdentifierOnePamResponse> involvedPartyInternalIdentifiers;

        private List<DigitalAddressOnePamResponse> digitalAddresses;

        private List<PostalAddressOnePamResponse> postalAddresses;

        private List<GroupResponse> involvedPartyGroups;

        private List<ManagingEntity> managingEntities;

        private String organisationLifeCycleStatusType;

        private LocalDate dateOfFoundation;

        private String legalForm;

        private String countryOfResidence;

        private String preferredLanguage;

        private String financialLegalStatusType;

        private LocalDate effectiveDate;

        private LocalDate endDate;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ManagingEntity {
            private String managingEntityType;
            private String managingEntityCode;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class GroupResponse {

            private String involvedPartyGroupType;

            private String involvedPartyGroupCode;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OrganisationNameOnePamResponse {
            @NotBlank
            private String organisationNameType;
            @NotBlank
            private String organisationName;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentifierOnePamResponse {
        private String involvedPartyInternalIdentifierType;
        private String involvedPartyInternalIdentifierValue;
        private String lastUpdateUser;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OFFSET_DATE_TIME_FORMAT)
        private OffsetDateTime lastUpdateDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostalAddressOnePamResponse {
        private String regionName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OFFSET_DATE_TIME_FORMAT)
        private OffsetDateTime effectiveDate;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OFFSET_DATE_TIME_FORMAT)
        private OffsetDateTime endDate;
        private String postalAddressUsageType;
        private String countryCode;
        private String countryRegionCode;
        private String cityName;
        private String postalCode;
        private String streetName;
        private String houseNumber;
        private String houseNumberAddition;
        private String buildingName;
        private String deliveryInformation;
        private String poBoxNumber;
        private String streetType;
        private String unstructuredAddressLine1;
        private String unstructuredAddressLine2;
        private String unstructuredAddressLine3;
        private String deliveryFailureReasonType;
        private String dataSource;
        private String lastUpdateUser;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OFFSET_DATE_TIME_FORMAT)
        private OffsetDateTime lastUpdateDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DigitalAddressOnePamResponse {

        private String fullDigitalAddress;

        private String digitalAddressUsageType;

        private String digitalAddressType;
    }
}


