package com.ing.bankguarantees.remote.rest.creditdecision;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput;
import com.ing.bankguarantees.remote.rest.creditdecision.model.response.CreditDecisionResponse;
import com.ing.bankguarantees.remote.rest.creditdecision.transformer.CreditDecisionReqTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * Configuration class for {@link JavaService JavaServices} that call Credit Risk API
 */
@Configuration
public class CreditDecisionConfig {


    @Bean("creditRiskRestClient")
    public JavaService<Request, CreditDecisionResponse> creditRiskRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
             RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                CreditDecisionResponse.class,
                ErrorSource.CRA);
    }

    @Bean("creditRiskGateway")
    public ClientGateway<CreditDecisionInput, CreditDecisionResponse, CreditDecisionResponse> creditRiskGateway(
             JavaService<Request, CreditDecisionResponse> creditRiskRestClient,
             CreditDecisionReqTransformer requestTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {
        return new ClientGateway<>(creditRiskRestClient, requestTransformer::transform,
                res -> new ResponseValidator<CreditDecisionResponse>(validator).validate(res), executorService);
    }
}
