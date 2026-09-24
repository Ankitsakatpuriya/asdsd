package com.ing.bankguarantees.remote.rest.bankaccountnumber.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountNumberRequest {
	private String requestId;
	private String accountType;
}