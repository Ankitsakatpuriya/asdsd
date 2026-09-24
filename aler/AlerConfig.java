package com.ing.bankguarantees.remote.rest.aler;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.aler.transformer.AlerRetrieveSignatoriesRequestTransformer;
import com.ing.bankguarantees.remote.rest.aler.transformer.AlerRetrieveSignatoriesResponseTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;

@Configuration
@Validated
public class AlerConfig {

    public JavaService<Request, AlerRetrieveSignatoriesResponse> alerRestClient(Service<Request, Response> httpClient, RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                AlerRetrieveSignatoriesResponse.class,
                ErrorSource.RMB);
    }

    @Bean("retrieveSignatoriesClientGateway")
    public ClientGateway<AlerRetrieveSignatoriesInput, AlerSignatories, AlerRetrieveSignatoriesResponse> retrieveSignatoriesClientGateway(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory,
            AlerRetrieveSignatoriesRequestTransformer requestTransformer,
            AlerRetrieveSignatoriesResponseTransformer responseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService) {

        return new ClientGateway<>(alerRestClient(httpClient, restServiceFactory), requestTransformer::transform,
                (res, in) -> responseTransformer.transform(res),
                new ResponseValidator<AlerRetrieveSignatoriesResponse>(validator)::validate, executorService);
    }

}
