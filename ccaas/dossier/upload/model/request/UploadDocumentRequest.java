package com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DossierDataRequest;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Request payload to create a request dossier in Commons Core as a Service
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest extends DossierDataRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("title")
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
