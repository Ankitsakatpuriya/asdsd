package com.ing.bankguarantees.remote.rest.emeafx.model.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FxRatesConversionInput(BigDecimal amount, String fromCurrency, String toCurrency) {

}
