package com.ing.bankguarantees.remote.rest.dar;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse.DarResponse;
import com.ing.bankguarantees.remote.rest.dar.transformers.DocumentSignRequestTransformer;
import com.ing.bankguarantees.remote.rest.dar.transformers.DocumentSignResponseTransformer;
import com.ing.bankguarantees.remote.rest.dar.transformers.DocumentSignV3RequestTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Validated
@Configuration
@RequiredArgsConstructor
public class DarConfig {

    @Bean("docSignRestClient")
    public JavaService<Request, DarListResponse> docSignRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
             RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                DarListResponse.class,
                ErrorSource.DAR);
    }

    @Bean("docSignGateway")
    public ClientGateway<DarInput, Optional<DarResponse>, DarListResponse> docSignGateway(
            JavaService<Request, DarListResponse> docSignRestClient,
            DocumentSignRequestTransformer docSignReqTransformer,
            DocumentSignResponseTransformer docSignResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(docSignRestClient, docSignReqTransformer::transform,
                (res, in) -> docSignResTransformer.transform(res),
                new ResponseValidator<DarListResponse>(validator)::validate, executorService);
    }

    @Bean("docSignV3Gateway")
    public ClientGateway<DarInput, Optional<DarResponse>, DarListResponse> docSignV3Gateway(
            JavaService<Request, DarListResponse> docSignRestClient,
            DocumentSignV3RequestTransformer docSignReqTransformer,
            DocumentSignResponseTransformer docSignResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(docSignRestClient, docSignReqTransformer::transform,
                (res, in) -> docSignResTransformer.transform(res),
                new ResponseValidator<DarListResponse>(validator)::validate, executorService);
    }
}
