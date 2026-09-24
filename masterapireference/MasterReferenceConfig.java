package com.ing.bankguarantees.remote.rest.masterapireference;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.masterapireference.model.MasterReferenceResponse;
import com.ing.bankguarantees.remote.rest.masterapireference.transformer.MasterReferenceReqTransformer;
import com.ing.bankguarantees.remote.rest.masterapireference.transformer.MasterReferenceRespTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@RequiredArgsConstructor
@Configuration
public class MasterReferenceConfig {


    @Bean("masterReferenceRestClient")
    public JavaService<Request, MasterReferenceResponse> masterReferenceRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                MasterReferenceResponse.class,
                ErrorSource.MRA);
    }

    @Bean("masterReferenceClientGateway")
    public ClientGateway<Void, String, MasterReferenceResponse> masterReferenceClientGateway(
            JavaService<Request, MasterReferenceResponse> masterReferenceRestClient,
            MasterReferenceReqTransformer masterReferenceReqTransformer,
            MasterReferenceRespTransformer masterReferenceRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(masterReferenceRestClient, masterReferenceReqTransformer::transform, (res, in) -> masterReferenceRespTransformer.transform(res),
                new ResponseValidator<MasterReferenceResponse>(validator)::validate, executorService);
    }
}
