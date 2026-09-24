package com.ing.bankguarantees.remote.rest.garcollaterals;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarResponse;
import com.ing.bankguarantees.remote.rest.garcollaterals.transformer.GarRequestTransformer;
import com.ing.bankguarantees.remote.rest.garcollaterals.transformer.GarResponseTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Configuration
@Slf4j
public class GarConfig {


    @Bean
    public JavaService<Request, GarResponse> garResponseRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
             RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                GarResponse.class,
                ErrorSource.GAC);
    }


    @Bean
    public ClientGateway<GarCollateralsInput, Optional<GarOutput>, GarResponse> garCollateralsGateway(
             JavaService<Request, GarResponse> garCollateralsClient,
             GarRequestTransformer requestTransformer,
             GarResponseTransformer responseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {
        return new ClientGateway<>(garCollateralsClient, requestTransformer::transform, responseTransformer::transform,
                new ResponseValidator<GarResponse>(validator)::validate, executorService);
    }


}
