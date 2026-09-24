package com.ing.bankguarantees.models;

import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankGuaranteeDataSet {

    private ArrayList<BankGuaranteeData> bankGuarantees ;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankGuaranteeData  {
        private BankGuaranteeCode bankGuaranteeCode;
        private String type;
        private String subType;
        private String typeNumber;
        private boolean inScope;
    }
}