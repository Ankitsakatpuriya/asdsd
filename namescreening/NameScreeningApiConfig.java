package com.ing.bankguarantees.remote.rest.namescreening;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningInput;
import com.ing.bankguarantees.remote.rest.namescreening.model.response.NameScreeningResponse;
import com.ing.bankguarantees.remote.rest.namescreening.transformer.NameScreeningReqTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@Configuration
public class NameScreeningApiConfig {


    @Bean("nameScreeningRestClient")
    public JavaService<Request, NameScreeningResponse> nameScreeningRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                NameScreeningResponse.class,
                ErrorSource.NSA);
    }

    @Bean("nameScreeningClientGateway")
    public ClientGateway<NameScreeningInput, NameScreeningResponse, NameScreeningResponse> nameScreeningClientGateway(
            JavaService<Request, NameScreeningResponse> nameScreeningRestClient,
            NameScreeningReqTransformer nameScreeningReqTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(nameScreeningRestClient, nameScreeningReqTransformer::transform, new ResponseValidator<NameScreeningResponse>(validator)::validate, executorService);
    }
}
