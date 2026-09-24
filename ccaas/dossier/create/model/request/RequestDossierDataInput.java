package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDossierDataInput {

    private String legalEntityId;
    private String requestId;
}
