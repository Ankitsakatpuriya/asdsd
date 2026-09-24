package com.ing.bankguarantees.remote.rest.pega.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PegaCreateCaseResponse(

        @JsonProperty("pxObjClass")
        String pxObjClass,

        @JsonProperty("ID")
        @NotBlank
        String id
) {
}