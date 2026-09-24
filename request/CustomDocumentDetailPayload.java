package com.ing.bankguarantees.models.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomDocumentDetailPayload {

    @NotBlank
    private String requestId;

    @NotBlank
    private List<String> uploadedFilesNames;
}