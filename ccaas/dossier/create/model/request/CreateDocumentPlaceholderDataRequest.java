package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentPlaceholderDataRequest extends DossierDataRequest {

    private String name;
    
    private String title;

    @JsonProperty("ing_language_code")
    private Set<String> ingLanguageCode;

    @JsonProperty("ing_ifw_category")
    private String ingIfwCategory;

    @JsonProperty("ing_doc_status")
    private IngDos ingDocStatus;

    @JsonProperty("ing_doc_type")
    private IngDos ingDocType;

    @JsonProperty("ing_doc_subtype")
    private IngDos ingDocSubtype;
}
