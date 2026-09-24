package com.ing.bankguarantees.remote.rest.namescreening.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameScreeningRequest {
    private String messageId;
    private String timestamp;
    private String gridId;
    private String userName;
    private String businessUnit;
    private String actorCountry;
    private String screeningType;
    private String alertGenerate;
    private String source;
    private Party party;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address{
        private String street;
        private String houseNumber;
        private String houseNumberAddition;
        private String zipCode;
        private String city;
        private String region;
        private String countryCode;
        private String countryDescription;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Party {
        private String partyId;
        private String partyType;
        private String firstName;
        private String lastName;
        private String fullName;
        private String nameType;
        private String birthDate;
        private String birthplace;
        private String organizationName;
        private Address address;
        private String countryIncCode;
        private String effectiveDate;
        private String endDate;
    }
}
