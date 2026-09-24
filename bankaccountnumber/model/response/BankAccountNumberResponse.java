package com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountNumberResponse {
	@NotBlank
	private String accountNumber;
	@NotBlank
	@NotNull private String accountType;
}