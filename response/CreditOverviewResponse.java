package com.ing.bankguarantees.models.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CreditOverviewResponse(
        Integer codeLanguage,
        Integer contraNotariety,
        Integer loanStatusManual,
        Integer segMis,
        Integer codeXy,
        Integer respCommercialManagerNo,
        String ssomi,
        BigDecimal availableAmount,
        Integer creditType,
        Integer status,
        List<IdentifierResponse> identifiers
) {
    @Builder
    public record IdentifierResponse(String type, String value) {
    }
}
