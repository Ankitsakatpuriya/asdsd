package com.ing.bankguarantees.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInformationPayload {

    @NotNull
    private AccountPayload accountToBeDebited;

    private ContractDetailPayload creditLine;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountPayload {

        @NotBlank
        private String uuid;

        @NotBlank
        private String accountName;

        @NotBlank
        private String ibanNumber;

        @NotBlank
        private String accountCurrency;

        @NotNull
        private BigDecimal balanceAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDetailPayload {

        @NotBlank
        private String uuid;

        @NotBlank
        private String accountName;

        @NotNull
        private BigDecimal creditLineAccountNumber;

        @NotNull
        private BigDecimal klcNumber;
        
        @NotBlank
        private String ibanNumber;

        @NotBlank
        private String currency;

        @NotEmpty
        private BigDecimal amount;


    }


}