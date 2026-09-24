package com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer;

import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AccountLoanReqTransformerTest {
    private static final String URL_FORMAT = "https://klc.api.ing.com/api/v2/account-loans/{contractNumber}";

    @Test
    void transformTest() {
        Request request = new AccountLoanReqTransformer(URL_FORMAT, "applicationName")
                .transform("contractNumber");
        assertThat(request.uri()).isEqualTo("/api/v2/account-loans/contractNumber");
        assertThat(request.method()).isEqualTo(Method.Get());
    }
}