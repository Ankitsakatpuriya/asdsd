package com.ing.bankguarantees.models.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
@Builder
public class EmptyConceptResponse {
    private String fileName;
    private ByteArrayResource fileContent;
}
