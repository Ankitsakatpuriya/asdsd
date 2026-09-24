package com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestAgreementDossierDataRequest {

    @JsonProperty("ing_application_owner_id")
    private String ingApplicationOwnerId;

    @JsonProperty("ing_dos_substatus")
    private IngDos ingDosSubstatus;  //request and agreement


}
