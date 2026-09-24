package com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request;

import com.ing.bankguarantees.models.enums.DocumentType;
import lombok.Builder;
import org.springframework.core.io.ByteArrayResource;

@Builder
public record UploadDocumentInput(

        String documentId,
        DocumentType documentType,
        ByteArrayResource fileContent,
        String language

) {
}