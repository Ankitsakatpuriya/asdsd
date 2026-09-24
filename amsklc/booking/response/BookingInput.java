package com.ing.bankguarantees.remote.rest.amsklc.booking.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BookingInput(String dataSource,
                           EventIdentifier eventIdentifier,
                           BigDecimal destinationOfFunds,
                           Integer mutationCode,
                           Integer statusLotAVOI,
                           Integer creationLinkCode,
                           Integer systemBookDate,
                           String rangeNumber,
                           Integer productCode,
                           AgreementIdentifier agreementIdentifier,
                           InitialAmount initialAmount) {
    @Builder
    public record EventIdentifier(String type,
                                  String value) {

    }
    @Builder
    public record AgreementIdentifier(String type,
                                      BigDecimal value){

    }
    @Builder
    public record InitialAmount(Currency currency,
                                @JsonProperty("Value")
                                double value){

        @Builder
        public record Currency(String code){

        }

    }
}
