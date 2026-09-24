package com.ing.bankguarantees.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryDatasetResponse {

    private List<CountryData> countryData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryData {
        private String businessKey;
        private String language;
        private String translation;
    }


}
