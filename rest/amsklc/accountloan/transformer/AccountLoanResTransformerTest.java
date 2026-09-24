package com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer;

import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AccountLoanResTransformerTest {

    @Test
    void transformTest() {
        AccountLoanResponse accountLoanResponse = MockHelper.getAccountLoanResponse();
        var accountLoanResTransformer = new AccountLoanResTransformer();
        var response = accountLoanResTransformer.transform(accountLoanResponse, "requestId");
        assertThat(response).isNotNull().isEqualTo(Optional.of(BigDecimal.valueOf(123456L)));
    }
}
