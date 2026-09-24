package com.ing.bankguarantees.remote.rest.accountbalance.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceRequest {

    @JsonProperty("iban_number")
    private String ibanNumber;

    @JsonProperty("account_product_id")
    private String accountProductId;

    @JsonProperty("account_currency")
    private String accountCurrency;


}
