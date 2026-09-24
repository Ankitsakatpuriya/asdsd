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
public class ApplicantDocumentPayload {

    @JsonProperty("company")
    private String companyName;

    @JsonProperty("kbo")
    private String kboNumber;
    private String street;

    private String city;

    private String zip;

    private String country;

    private ApplicantContactDocumentPayload contact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantContactDocumentPayload {

        private String name;
        private String phone;
        private String email;

    }

}
