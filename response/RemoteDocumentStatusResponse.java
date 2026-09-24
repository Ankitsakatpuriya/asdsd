package com.ing.bankguarantees.models.response;

import com.ing.bankguarantees.models.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteDocumentStatusResponse {
    private DocumentStatus status;
}
