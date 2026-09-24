package com.ing.bankguarantees.remote.rest.amsklc.booking.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BookingRequest(String reservationNumber, BigDecimal accountNumber, RequestType requestType,
                             BigDecimal klcNumber, double bgAmount, String currency) {
}
