package com.ing.bankguarantees.remote.rest.creditdecision.model.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDecisionResponse {

    @Valid
    @NotNull
    private DecisionResult decisionResult;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionResult {
        @NotBlank
        private String automaticDecision;
    }

}