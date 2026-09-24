package com.ing.bankguarantees.models.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryDataCache {
    private String businessKey;
    private String language;
    private String translation;
}
