package com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer;


import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Component
public class CreditLineResTransformer implements InputTransformer<CreditLineBalanceResponse, CreditBalanceInput, List<CreditBalanceOutput>> {


    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Override
    public List<CreditBalanceOutput> transform(CreditLineBalanceResponse creditLineBalanceResponse,
                                               CreditBalanceInput creditBalanceInput) {
        log.info("CreditLineResTransformer [transform ] Receive response for Credit Line Balance API ");

        return creditLineBalanceResponse.getContractDetails()
                .stream()
                .map(creditAccount -> CreditBalanceOutput.builder()
                        .klcNumber(creditAccount.getContractNumber())
                        .accountName(creditBalanceInput.getAccountName())
                        .uuid(creditBalanceInput.getUuid())
                        .endDate(creditAccount.getEndDate() != null ? LocalDate.parse(creditAccount.getEndDate(), formatter) : null)
                        .creditLineAccountNumber(BigDecimal.valueOf((creditBalanceInput.getCreditLineAccountNumber())))
                        .productCode(creditAccount.getProductCode())
                        .rangeStatus(creditAccount.getRangeStatus())
                        .availableAmount(CreditBalanceOutput.AvailableAmountOutput.builder()
                                .amount(creditAccount.getAvailableAmount().getAmount())
                                .currency(creditAccount.getAvailableAmount().getCurrency())
                                .decimal(creditAccount.getAvailableAmount().getDecimal())
                                .build())
                        .build())
                .toList();

    }
}
