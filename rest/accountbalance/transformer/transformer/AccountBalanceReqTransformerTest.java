package com.ing.bankguarantees.remote.rest.accountbalance.transformer.transformer;

import com.ing.bankguarantees.remote.rest.accountbalance.transformer.AccountBalanceReqTransformer;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@ExtendWith(MockitoExtension.class)
class AccountBalanceReqTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/api/accounts/balances/search";


    @Test
    void transformTest() {

        Request request = new AccountBalanceReqTransformer(URL_FORMAT, "003").
                transform(RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement()));
        assertThat(request.uri()).isEqualTo("/api/accounts/balances/search");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isEqualTo("[{\"iban_number\":\"BE1267354572\",\"account_product_id\":\"1000\",\"account_currency\":\"003\"}]");
    }
}
