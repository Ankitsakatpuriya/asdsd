package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request;

import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import lombok.Builder;

@Builder
public record CreditLineArrangementRequest(String reservationNumber, RequestType requestType,
                                           double bgAmount,
                                           String accountNumber,
                                           String creditMode,
                                           String currency) {
}
