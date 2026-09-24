package com.ing.bankguarantees.remote.rest.accountbalance.model.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceInput {

    private String accountName;
    private String uuid;
    private String accountCurrency;
    private String ibanNumber;
}
