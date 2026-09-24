package com.ing.bankguarantees.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDataSet {

    private List<CurrencyData> currencies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyData {
        private String entryId;
        private String tableName;
        private String code;
        private String valueNL;
        private String valueFR;
        private String valueEN;
    }

}
