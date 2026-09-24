package com.ing.bankguarantees.remote.rest.emeafx;

import com.ing.bankguarantees.remote.rest.emeafx.model.request.FxRatesConversionInput;
import com.ing.bankguarantees.remote.rest.emeafx.transformer.FxRateConversionRequestTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FxRateConversionRequestTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/payments/forex/emea/generic-rates/convert";
    private static final String FROM_CURRENCY = "GBP";
    private static final String TO_CURRENCY = "EUR";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(30.50);

    @Test
    void transformTest() {
        FxRatesConversionInput fxReteConversionInput = MockHelper.getFxReteConversionInput(AMOUNT, FROM_CURRENCY, TO_CURRENCY);
        Request request = new FxRateConversionRequestTransformer(URL_FORMAT).transform(fxReteConversionInput);
        assertThat(request.uri()).contains("/payments/forex/emea/generic-rates/convert");
        assertThat(request.method()).isEqualTo(Method.Get());
        assertThat(request.getParam("fromCurrency")).isEqualTo(FROM_CURRENCY);
        assertThat(request.getParam("toCurrency")).isEqualTo(TO_CURRENCY);
        assertThat(request.getParam("amount")).isEqualTo(AMOUNT.toPlainString());
    }
}