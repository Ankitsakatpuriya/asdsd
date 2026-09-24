package com.ing.bankguarantees.remote.rest.creditdecision.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditRiskRequest {

    private BigInteger requestId;
    private String requestDate;
    private String productType;
    private String requestChannel;
    private String requestType;
    private String openingBranch;
    private String followupBranch;
    private boolean professionalUseFlag;
    private String agreementCurrency;
    private List<CreditOperation> creditOperations;
    private List<InvolvedParty> involvedparties;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreditOperation {

        private String productNatureType;
        private BigDecimal creditAmount;
        private Integer durationMonths;
        private Integer capitalPaymentFrequency;
        private String privateUse;
        private String operationProductType;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvolvedPartyInternalIdentifier {
        private String id;
        private String type;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvolvedParty {
        private InvolvedPartyInternalIdentifier involvedPartyInternalIdentifier;
        private Integer workStability;
        private String partnershipType;
        private String intervenientType;
    }

}