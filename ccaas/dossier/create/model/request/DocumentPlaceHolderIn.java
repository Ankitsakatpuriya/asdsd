package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request;

import com.ing.bankguarantees.models.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * DTO object to create the Document Placeholder Request Payload for ConnectDotAPI
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPlaceHolderIn {

    @NotBlank
    private String dossierId;

    private DocumentType documentType;

    @NotBlank
    private Locale locale;
}
