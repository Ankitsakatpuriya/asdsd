package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Request payload to create a request dossier in Commons Core as a Service
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDossierDataRequest extends DossierDataRequest {

    /**
     * Identifier values of the organisation
     */
    @JsonProperty("ing_reference_id")
    private List<String> ingReferenceId;

    /**
     * Identifier types of the organisation
     */
    @JsonProperty("ing_party_type")
    private List<String> ingPartyType;

    @JsonProperty("ing_dos_type")
    private IngDos ingDosType;

    @JsonProperty("ing_dos_subtype")
    private IngDos ingDosSubtype;

    @JsonProperty("ing_dos_substatus")
    private IngDos ingDosSubstatus;

    /**
     * loan application id
     */
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("reporting_entity")
    private String reportingEntity;


}
