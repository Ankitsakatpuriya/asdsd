package com.ing.bankguarantees.models.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDataCache {
    private String entryId;
    private String tableName;
    private String code;
    private String valueNL;
    private String valueFR;
    private String valueEN;
}