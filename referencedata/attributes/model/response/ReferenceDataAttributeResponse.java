package com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Response model for Reference Data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceDataAttributeResponse {

    @NotNull
    private List<AttributeDataResponse> data;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeDataResponse {

        @NotBlank
        @JsonProperty("business_key")
        private String businessKey;

        @JsonProperty("end_dt")
        private LocalDate endDate;

        @NotBlank
        @JsonProperty("values")
        private List<AttributeColumnDataResponse> columnDataList;


    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeColumnDataResponse {

        @NotBlank
        @JsonProperty("column")
        private String columnName;
        @NotBlank
        private String value;
    }
}
