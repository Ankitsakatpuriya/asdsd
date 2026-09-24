package com.ing.bankguarantees.remote.rest.emeafx.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.emeafx.model.request.FxRatesConversionInput;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FxRateConversionRequestTransformer extends RequestTransformer<FxRatesConversionInput> {

    private static final String RATE_TYPE_HEADER = "rateType";
    private static final String AMOUNT_HEADER = "amount";
    private static final String TO_CURRENCY_HEADER = "toCurrency";
    private static final String FROM_CURRENCY_HEADER = "fromCurrency";
    private static final String RATE_TYPE_VALUE = "ASK";

    public FxRateConversionRequestTransformer(@Value("${rest.payments.forex.emea.generic-rates.convert.url}")
                                              @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(FxRatesConversionInput input) {


        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(getUrlFormat())
                .addQueryParam(AMOUNT_HEADER, input.amount().toPlainString())
                .addQueryParam(RATE_TYPE_HEADER, RATE_TYPE_VALUE)
                .addQueryParam(FROM_CURRENCY_HEADER, input.fromCurrency())
                .addQueryParam(TO_CURRENCY_HEADER, input.toCurrency())
                .build();

        log.info(C3LogMarker.marker, """
                PaymentsForexEmeaGenericRates API: Calling POST {} endpoint[{}] to convert amount currency,
                """, getUrlFormat(), request.hashCode());
        return request;
    }

}
