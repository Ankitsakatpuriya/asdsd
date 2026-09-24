package com.ing.bankguarantees.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomDocumentDetailData {

    private String requestId;
    private String dossierId;
    private String documentId;
    private String agreementDossierId;
    private List<String> uploadedFilesNames;
}