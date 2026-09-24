package com.ing.bankguarantees.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDetailResponse {

    private FinancialCreditLineResponse creditLineDetails;

    private FinancialCurrentAccountResponse currentAccountDetails;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialCurrentAccountResponse {

        private List<FinancialAccountResponse> accounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialCreditLineResponse{

        private List<FinancialContractDetailResponse> contractDetails;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialAccountResponse {

        private String uuid;
        private String accountName;
        private Integer accountStatus;
        private String ibanNumber;
        private String accountCurrency;
        private BigDecimal balanceAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialContractDetailResponse {


        private String uuid;
        private String accountName;
        private BigDecimal klcNumber;
        private BigDecimal creditLineAccountNumber;
        private String ibanNumber;
        private String currency;
        private BigDecimal balanceAmount;
        private BigDecimal granteeAmount;
        @JsonFormat(pattern = "yyyyMMdd")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate endDate;    }
}

