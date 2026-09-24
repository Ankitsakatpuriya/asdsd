package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class CreateAgreementDossierDataRequest extends DossierDataRequest {

    private String title;

    /**
     * contract number
     */
    @JsonProperty("ing_reference_id")
    private Set<String> ingReferenceId;

    @JsonProperty("ing_party_type")
    private Set<String> ingPartyType;

    @JsonProperty("ing_dos_type")
    private IngDos ingDosType;

    @JsonProperty("ing_dos_subtype")
    private IngDos ingDosSubtype;

    @JsonProperty("ing_dos_substatus")
    private IngDos ingDosSubstatus;

    /**
     * request dossier id
     */
    @JsonProperty("ing_dos_groupid")
    private String ingDosGroupId;

}
