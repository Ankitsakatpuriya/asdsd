package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DossierType {
    REQUEST_DOSSIER("request-dossiers"),
    AGREEMENT_DOSSIER("agreement-dossiers"),
    DOCUMENT_PLACEHOLDER_DOSSIER("documents");

    private final String docType;

}
