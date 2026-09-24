package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceActivitiesCode {

    ORGANIZATION("legalEntityId"),
    FINANCIAL("legalEntityId"),
    SUBMIT("bankGuaranteeRequestPayload"),
    GET_DOCUMENT("requestId"),
    SIGNING("signDocumentPayload"),
    FINALIZATION("finalizationPayload"),
    SIGNER_SELECTION("signatoriesSelectionPayload"),
    DOSSIER_UPDATE("dossierUpdatePayload");
    private final String parameterName;

}

