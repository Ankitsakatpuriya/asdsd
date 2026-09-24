package com.ing.bankguarantees.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankGuaranteeFinalizationResponse {
    private String requestId;

}
