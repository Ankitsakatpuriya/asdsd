package com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CreditBalanceOutput {
    private String uuid;
    private String accountName;
    private BigDecimal klcNumber;
    private BigDecimal creditLineAccountNumber;
    private AvailableAmountOutput availableAmount;
    private LocalDate endDate;
    private Integer rangeStatus;
    private Integer productCode;

    @Builder
    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class AvailableAmountOutput {

        private String currency;
        private BigDecimal amount;
        private Integer decimal;

    }
}
