package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;

import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer.ExtIdentReqTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer.ExtIdentRespTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.ExecutorService;


/**
 * Configuration class for {@link JavaService JavaServices} that call  API
 */
@Configuration
public class ExternalIdentifierApiConfig {

    @Bean("extIdentifierRestClient")
    public JavaService<Request, ExternalIdentifierListResponse> extIdentifierRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                ExternalIdentifierListResponse.class,
                ErrorSource.IPA);
    }

    @Bean("extIdentifierGateway")
    public ClientGateway<String, Optional<String>, ExternalIdentifierListResponse> extIdentifierGateway(
            JavaService<Request, ExternalIdentifierListResponse> extIdentifierRestClient,
            ExtIdentReqTransformer extIdentReqTransformer,
            ExtIdentRespTransformer extIdentRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(extIdentifierRestClient, extIdentReqTransformer::transform,
                (res, in) -> extIdentRespTransformer.transform(res), new ResponseValidator<ExternalIdentifierListResponse>(validator)::validate, executorService);
    }
}
