package com.ing.bankguarantees.remote.rest.amsklc.accountloan.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;

@Builder
public record AccountLoanResponse(List<FacilityAgreementItem> facilityAgreement){
    @Builder
    public record FacilityAgreementItem(
            LendingLimitAmount lendingLimitAmount) {
        @Builder
        public record LendingLimitAmount(Currency currency, BigDecimal value) {

            @Builder
            public record Currency(String code) {
            }
        }
    }
}