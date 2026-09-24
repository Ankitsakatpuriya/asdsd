package com.ing.bankguarantees.remote.rest.pega;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseResponse;
import com.ing.bankguarantees.remote.rest.pega.transformer.PegaCreateCaseReqTransformer;
import com.ing.bankguarantees.remote.rest.pega.transformer.PegaCreateCaseRespTransformer;
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
public class PegaConfig {


    @Bean("pegaCaseCreationRestClient")
    public JavaService<Request, PegaCreateCaseResponse> pegaCaseCreationRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                PegaCreateCaseResponse.class,
                ErrorSource.PEGA
        );
    }


    @Bean("pegaCaseCreationGateway")
    public ClientGateway<PegaCreateCaseInput, String, PegaCreateCaseResponse> pegaCaseCreationGateway(
            JavaService<Request, PegaCreateCaseResponse> pegaCaseCreationRestClient,
            PegaCreateCaseReqTransformer requestTransformer,
            PegaCreateCaseRespTransformer pegaCreateCaseRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService) {
        return new ClientGateway<>(pegaCaseCreationRestClient, requestTransformer::transform, (res, in) -> pegaCreateCaseRespTransformer.transform(res),
                res -> new ResponseValidator<PegaCreateCaseResponse>(validator).validate(res), executorService);
    }

}
