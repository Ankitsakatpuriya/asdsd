package com.ing.bankguarantees.remote.rest.intake;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.response.IntakeApiResponse;
import com.ing.bankguarantees.remote.rest.intake.transformers.IntakeApiRequestTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

@Configuration
@RequiredArgsConstructor
public class IntakeApiConfig {

    @Bean("intakeApiRestClient")
    public JavaService<Request, IntakeApiResponse> intakeApiRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                IntakeApiResponse.class,
                ErrorSource.ABG);
    }

    @Bean("intakeApiGateway")
    public ClientGateway<IntakeApiInput, IntakeApiResponse, IntakeApiResponse> intakeApiGateway(
            JavaService<Request, IntakeApiResponse> intakeApiRestClient,
            IntakeApiRequestTransformer intakeApiRequestTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(intakeApiRestClient, intakeApiRequestTransformer::transform,
                new ResponseValidator<IntakeApiResponse>(validator)::validate, executorService);
    }
}
