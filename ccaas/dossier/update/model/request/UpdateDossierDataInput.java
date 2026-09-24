package com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request;

import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DossierType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDossierDataInput {
    String dossierId;
    String status;
    DossierType dossierType;
}