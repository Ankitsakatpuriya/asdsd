package com.ing.bankguarantees.remote.rest.connectdot;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.response.ConnectDotResponse;
import com.ing.bankguarantees.remote.rest.connectdot.transformer.ConnectDotReqTransformer;
import com.ing.bankguarantees.remote.rest.connectdot.transformer.ConnectDotResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;


@Validated
@Configuration
public class ConnectDotConfig {


    @Bean("connectDotRestClient")
    public JavaService<Request, ConnectDotResponse> connectDotRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                ConnectDotResponse.class,
                ErrorSource.CDA);
    }


    @Bean("connectDotApiClientGateway")
    public ClientGateway<ConnectDotInput, Boolean, ConnectDotResponse> connectDotApiClientGateway(
             JavaService<Request, ConnectDotResponse> connectDotRestClient,
             ConnectDotReqTransformer connectDotReqTransformer,
             ConnectDotResTransformer connectDotResTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService
    ) {

        return new ClientGateway<>(connectDotRestClient, connectDotReqTransformer::transform, (res, in) -> connectDotResTransformer.transform(res),
                new ResponseValidator<ConnectDotResponse>(validator)::validate, executorService);
    }
}
