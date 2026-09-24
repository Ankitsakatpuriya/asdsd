
package com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;


@Builder
public record CustomerArrangementResponse(

        @JsonProperty("SignaleticInformation")
        List<SignaleticInformationResponse> signaleticInformation
) {

    @Builder
    public record SignaleticInformationResponse(
            Integer codeLanguage,
            Integer contraNotariety,
            Integer loanStatusManual,
            Integer segMis,
            Integer codeXy,
            String gridId,
            Integer respCommercialManagerNo,
            String involvedPartyIdentifier,
            String involvedPartyItvIdentifier,
            String ssomi
    ) {
    }
}

