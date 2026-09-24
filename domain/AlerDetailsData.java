package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerDetailsData {


    private String transactionId;
    private String transactionStatus;
    private boolean invokeAler;
    private List<String> signers;
    private AlerErrorDetailData alerErrorDetailData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlerErrorDetailData {

        private String code;
        private String message;
    }

}