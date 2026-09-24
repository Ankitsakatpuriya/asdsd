
package com.ing.bankguarantees.remote.rest.accountbalance.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceOutput{
    private Integer accountStatus;
    private String ibanNumber;
    private String accountCurrency;
    private BigDecimal balanceAmount;
    private String accountProductId;
    private String accountName;
    private String uuid;
}

