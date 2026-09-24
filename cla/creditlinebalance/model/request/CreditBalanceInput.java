package com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceInput {
    private String accountCurrency;
    private Long creditLineAccountNumber;
    private String uuid;
    private String accountName;
}
