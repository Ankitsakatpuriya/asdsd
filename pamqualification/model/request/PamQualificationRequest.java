package com.ing.bankguarantees.remote.rest.pamqualification.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PamQualificationRequest {

    private String operation;
    private PartyRequest beneficiary;
    private PartyRequest requester;
    private String ingEntity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyRequest {
        public String id;
        public String type;
    }
}
