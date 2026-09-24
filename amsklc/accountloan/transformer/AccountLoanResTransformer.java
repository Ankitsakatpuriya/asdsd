package com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer;


import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;


@Slf4j
@Component("acctLoanRespTransformer")
public class AccountLoanResTransformer implements InputTransformer<AccountLoanResponse, String, Optional<BigDecimal>> {

    @Override
    public Optional<BigDecimal> transform(AccountLoanResponse accountLoanResponse, String contractNumber) {
        log.info("AccountBalanceResTransformer [transform] receive response for Account balance Search API {} ", accountLoanResponse);

        return (!accountLoanResponse.facilityAgreement().isEmpty() &&
                accountLoanResponse.facilityAgreement().get(0).lendingLimitAmount() != null)
                ? Optional.of(accountLoanResponse.facilityAgreement().get(0).lendingLimitAmount().value())
                : Optional.empty();
    }
}
