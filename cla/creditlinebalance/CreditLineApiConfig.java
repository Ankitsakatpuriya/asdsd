package com.ing.bankguarantees.remote.rest.cla.creditlinebalance;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineReqTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Configuration
@Validated
public class CreditLineApiConfig {

    @Bean("creditLineRestClient")
    public JavaService<Request, CreditLineBalanceResponse> creditLineRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                CreditLineBalanceResponse.class,
                ErrorSource.CLAA);
    }

    @Bean("creditLineClientGateway")
    public ClientGateway<CreditBalanceInput, List<CreditBalanceOutput>, CreditLineBalanceResponse> creditLineClientGateway(
             JavaService<Request, CreditLineBalanceResponse> creditLineRestClient,
             CreditLineReqTransformer creditLineReqTransformer,
             CreditLineResTransformer creditLineResTransformer,
             Validator validator,
             @Qualifier("workStealingPool")ExecutorService executorService
    ) {
        return new ClientGateway<>(creditLineRestClient, creditLineReqTransformer::transform, creditLineResTransformer::transform,
                new ResponseValidator<CreditLineBalanceResponse>(validator)::validate, executorService);
    }
}
