package com.ing.bankguarantees.remote.rest.aler.model.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record AlerRetrieveSignatoriesResponse(


        @NotNull
        String transactionId,
        String transactionStatus,
        ErrorResponse errorResponseDetails,
        SignatoryResponse signatoryResponse
) {


    @Builder
    public record SignatoryResponse(

            Integer purposeId,
            boolean onlyActiveSignatories,
            String requestorIdentifier,

            @NotBlank
            String organisationIdentifier,

            String organisationName,
            @NotEmpty
            List<Signatory> signatories
    ) {
    }

    @Builder
    public record ErrorResponse(
            String serviceOrigin,
            String code,
            String message
    ) {
    }

    @Builder
    public record Signatory(
            String individualIdentifier,
            String individualName,
            boolean isActive,
            SigningPower signingPower
    ) {
    }

    @Builder
    public record SigningPower(@NotNull Integer signingPowerValue, String signingPowerDescription) {
    }


}
