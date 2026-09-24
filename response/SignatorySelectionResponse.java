package com.ing.bankguarantees.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatorySelectionResponse {

    private String transactionId;

    private boolean invokeAler;
}
