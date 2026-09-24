package com.ing.bankguarantees.remote.rest.pamqualification;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import com.ing.bankguarantees.remote.rest.pamqualification.transformer.PamQualificationReqTransformer;
import com.ing.bankguarantees.remote.rest.pamqualification.transformer.PamQualificationResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@Configuration
public class PamQualificationConfig {


    @Bean("pamQualificationRestClient")
    public JavaService<Request, PamQualificationResponse> pamQualificationRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                PamQualificationResponse.class,
                ErrorSource.PQC
        );
    }


    @Bean("pamQualificationGateway")
    public ClientGateway<String, Boolean, PamQualificationResponse> pamQualificationGateway(
            JavaService<Request, PamQualificationResponse> pamQualificationRestClient,
            PamQualificationReqTransformer requestTransformer,
            PamQualificationResTransformer responseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService) {
        return new ClientGateway<>(pamQualificationRestClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<PamQualificationResponse>(validator).validate(res), executorService);
    }

}
