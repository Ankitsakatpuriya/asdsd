package com.ing.bankguarantees.remote.rest.namescreening.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Input for Name Screening Transformer
 */
@Data
@Builder
@AllArgsConstructor
public class NameScreeningInput {
    private String organizationName;
    private String street;
    private String houseNumber;
    private String zipCode;
    private String city;
    private String countryCode;
    private String countryDescription;
    private String partyType;
    private String fullName;
    private LocalDate dateOfBirth;
}
