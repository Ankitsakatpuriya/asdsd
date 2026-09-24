package com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for Reference Data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataMultilingualResponse {

    @NotNull
    private List<ReferenceData> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceData {

        @NotBlank
        @JsonProperty("business_key")
        private String businessKey;
        @NotBlank
        private String language;
        @NotBlank
        private String translation;
    }
}
