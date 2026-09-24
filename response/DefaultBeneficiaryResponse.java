package com.ing.bankguarantees.models.response;

import lombok.Builder;

@Builder
public record DefaultBeneficiaryResponse(
        String pageDescription,
        String email,
        String name,
        String firstAddress,
        String postalCode,
        String cityName,
        String countryCode) {
}
