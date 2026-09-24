package com.ing.bankguarantees.remote.rest.pega.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PegaCreateCaseRequest(

        @JsonProperty("caseTypeID")
        String caseTypeId,

        @JsonProperty("processID")
        String processId,

        @JsonProperty("content")
        Content content
) {
}