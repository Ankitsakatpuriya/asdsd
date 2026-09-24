package com.ing.bankguarantees.remote.rest.intake.model.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record IntakeApiResponse(

        String id,

        @NotBlank
        String pegaCaseId,

        @NotBlank
        String masterReference

) {

}
