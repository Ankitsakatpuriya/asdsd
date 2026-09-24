package com.ing.bankguarantees.remote.rest.emeafx;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.emeafx.model.request.FxRatesConversionInput;
import com.ing.bankguarantees.remote.rest.emeafx.model.response.FxRateConversionResponse;
import com.ing.bankguarantees.remote.rest.emeafx.transformer.FxRateConversionRequestTransformer;
import com.ing.bankguarantees.remote.rest.emeafx.transformer.FxRateConversionResponseTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;

@Configuration
@Validated
public class EmeaForexConfig {

    public JavaService<Request, FxRateConversionResponse> fxRateConversionRestClient(Service<Request, Response> httpClient,
                                                                                     RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                FxRateConversionResponse.class,
                ErrorSource.PFEGR);
    }

    @Bean("fxRateConversionClientGateway")
    public ClientGateway<FxRatesConversionInput, BigDecimal, FxRateConversionResponse> fxRateConversionClientGateway(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory,
            FxRateConversionRequestTransformer requestTransformer,
            FxRateConversionResponseTransformer responseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService) {

        return new ClientGateway<>(fxRateConversionRestClient(httpClient, restServiceFactory), requestTransformer::transform,
                (res, in) -> responseTransformer.transform(res),
                new ResponseValidator<FxRateConversionResponse>(validator)::validate, executorService);
    }

}
