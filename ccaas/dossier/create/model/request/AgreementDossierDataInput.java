package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgreementDossierDataInput {
    private String reqResponseId;
    private String requestId;
    private String legalEntityId;
}
