package com.ing.bankguarantees.remote.rest.dar.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Translations {

    @JsonProperty("BANK_GUARANTEE")
    private TranslationRequest bankGuarantee;

    @JsonProperty("COLLATERAL")
    private TranslationRequest collateral;

    @JsonProperty("CONTRACT")
    private TranslationRequest contract;

    @JsonProperty("FILLED_IN_CONCEPT")
    private TranslationRequest filledInConcept;

    @JsonProperty("SIGNATORY_NOTICE")
    private TranslationRequest signatoryNotice;

    @JsonProperty("FRONTEND_CALLBACK")
    private TranslationRequest frontendCallback;

    @JsonProperty("AGREEMENT_ID_REPLACER")
    private TranslationRequest agreementIdReplacer;


}
