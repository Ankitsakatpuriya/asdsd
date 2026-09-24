package com.ing.bankguarantees.remote.rest.accountbalance.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountBalanceResponse {

    @JsonProperty("positive_results")
    private Integer positiveResults;

    @JsonProperty("missing_results")
    private Integer missingResults;

    @NotNull
    private List<AccountResponse> accounts;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountResponse {

        @JsonProperty("account_status")
        private Integer accountStatus;

        @NotBlank
        @JsonProperty("iban_number")
        private String ibanNumber;


        @JsonProperty("account_currency")
        private String accountCurrency;

        @NotNull
        @JsonProperty("balance_amount")
        private BigDecimal balanceAmount;

        @JsonProperty("account_product_id")
        private String accountProductId;

        @NotBlank
        @JsonProperty("balance_currency")
        private String balanceCurrency;

        @JsonProperty("credit_line_amount")
        private BigDecimal creditLineAmount;

        @JsonProperty("credit_line_currency")
        private String creditLineCurrency;

        @JsonProperty("variable_amount")
        private BigDecimal variableAmount;

        @JsonProperty("variable_currency")
        private String variableCurrency;

        @JsonProperty("domestic_balance_amount")
        private BigDecimal domesticBalanceAmount;

        @JsonProperty("domestic_balance_currency")
        private String domesticBalanceCurrency;

        @JsonProperty("domestic_credit_line_amount")
        private BigDecimal domesticCreditLineAmount;

        @JsonProperty("domestic_credit_line_currency")
        private String domesticCreditLineCurrency;

        @JsonProperty("domestic_variable_amount")
        private BigDecimal domesticVariableAmount;

        @JsonProperty("domestic_variable_currency ")
        private String domesticVariableCurrency;
    }

}
