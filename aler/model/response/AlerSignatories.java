package com.ing.bankguarantees.remote.rest.aler.model.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AlerSignatories(

        String transactionId,
        String transactionStatus,
        AlerErrorDetails errorDetails,
        List<Signatory> signatories
) {

    @Builder
    public record Signatory(String signatoryUUID, boolean isActive, int signingPower) {
    }

    @Builder
    public record AlerErrorDetails(String code, String message) {

    }
}
