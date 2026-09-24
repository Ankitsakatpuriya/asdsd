
package com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response;

import lombok.Builder;


@Builder
public record CustomerArrangementOutput(
        Integer codeLanguage,
        Integer contraNotariety,
        Integer loanStatusManual,
        Integer segMis,
        Integer codeXy,
        String gridId,
        Integer respCommercialManagerNo,
        String ssomi,
        String csiIdentifier,
        String itvIdentifier
) {

}

