package com.ing.bankguarantees.models.response;

import lombok.Builder;

@Builder
public record BeneficiaryWarningResponse(String cbeNo, String warning) {
}

