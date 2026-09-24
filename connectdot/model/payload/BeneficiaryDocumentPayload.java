package com.ing.bankguarantees.remote.rest.connectdot.model.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryDocumentPayload {

    @JsonProperty("name")
    private String companyName;

    @JsonProperty("kbo")
    private String kboNumber;
    private String street;
    private String city;
    private String zip;
    private String country;
    private String email;
    private String countryCode;
    private String id;

    @JsonProperty("secondaryName")//secondary beneficiary name in DCK private individual
    private String secondaryBeneficiaryName;
    @JsonProperty("secondaryId")//secondary beneficiary Id in DCK private individual
    private String secondaryBeneficiaryId;


}
