package com.ing.bankguarantees.remote.rest.garcollaterals.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import com.ing.bankguarantees.models.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarOutput {
    private List<Collateral> collaterals;
    private List<Covenant> covenants;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Covenant {
        private String agreementType;
        @JsonFormat(pattern = "yyyyMMdd")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate date;
        private GarAmount garAmount;
        private List<Collateral.GarParty> grantedBy;
        private String generalCoverType;
        private String comments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Collateral {
        private String collateralType;
        @JsonFormat(pattern = "yyyyMMdd")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate date;

        private GarAmount garAmount;
        private List<GarParty> grantedBy;
        private String generalCoverType;
        private String assertDescription;
        private String comments;

        @Data
        @Builder
        public static class GarParty {
            private Identifier identifier;
            private String name;
        }
    }

    @Data
    @Builder
    public static class GarAmount {

        private BigDecimal value;
        private String currency;
    }

}
