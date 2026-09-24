package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvolvedPartyCsiRequest {


    private InvolvedPartyRegistration involvedPartyRegistration;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvolvedPartyRegistration {

        private int registrationAuthority;
        private String registrationNumber;
    }

}

