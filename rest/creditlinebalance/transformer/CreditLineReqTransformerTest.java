package com.ing.bankguarantees.remote.rest.creditlinebalance.transformer;

import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineReqTransformer;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreditLineReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/api/lending/get/creditLineBalance";
    private static final String APPLICATION_NAME = "BankGuaranteesBEOnline_API";

    @Mock
    private static CurrencyDetailService currencyDetailService;


    @Test
    void transformTest() {
        var creditLineReqTransformer = new CreditLineReqTransformer(URL_FORMAT, APPLICATION_NAME, "003");
        var creditLineRequest = RequestAdapter.getCreditLineRequest(MockHelper.getCreditLineProductAgreement());
        Request request = creditLineReqTransformer.transform(creditLineRequest);
        assertThat(request.uri()).isEqualTo("/api/lending/get/creditLineBalance");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.getContentString()).isEqualTo("{\"productCode\":6000,\"accountNumber\":384031884085,\"currency\":3}");
    }

}
