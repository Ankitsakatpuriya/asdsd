package com.ing.bankguarantees.remote.rest.amsklc.booking.response;


import lombok.Builder;

@Builder
public record BookingResponse(int returnCode,
                                  String eventIdentifier,
                                  AgreementIdentifier agreementIdentifier,
                                  String rangeNumber,
                                  int mutationCode,
                                  String dataSource) {
    @Builder
    public record AgreementIdentifier(String type,
                               String value){

    }
}
