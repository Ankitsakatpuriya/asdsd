
package com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * CreditLineBalanceRequest
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditLineBalanceRequest {

    private Integer productCode;

    private Long accountNumber;

    private Integer currency;

}

