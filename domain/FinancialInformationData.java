package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInformationData {


    private AccountData accountToBeDebited;

    private ContractDetailData creditLine;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountData {

        private String accountName;

        private String uuid;

        private String ibanNumber;

        private String accountCurrency;

        private BigDecimal balanceAmount;

        private String panNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDetailData {

        private String uuid;

        private String accountName;

        private BigDecimal creditLineAccountNumber;

        private BigDecimal klcNumber;

        private String ibanNumber;

        private String currency;

        private BigDecimal amount;

        private LocalDate endDate;
    }


}