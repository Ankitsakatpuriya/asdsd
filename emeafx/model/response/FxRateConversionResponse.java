package com.ing.bankguarantees.remote.rest.emeafx.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FxRateConversionResponse(

        @NotNull
        BigDecimal convertedAmount,
        BigDecimal amount,
        String toCurrency,
        String fromCurrency,
        String rateType
) {

}
