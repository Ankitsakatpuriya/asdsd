package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.CSIHubInvolvedPartiesRequest;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response.InvolvedPartiesCsiHubResponse;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.transformer.InvolvedPartyCsiReqTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@Configuration
public class CSIHubConfig {

    /**
     * Create a {@link JavaService} that calls CSI HUB API using the {@code restServiceFactory}.
     *
     * @param httpClient         HttpClient
     * @param restServiceFactory Rest Service factory
     * @return javaService
     */
    @Bean("csiInvolvePartyClient")
    public JavaService<Request, InvolvedPartiesCsiHubResponse> csiInvolvePartyClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                InvolvedPartiesCsiHubResponse.class,
                ErrorSource.CHA);
    }


    @Bean("csiInvolvePartyGateway")
    public ClientGateway<CSIHubInvolvedPartiesRequest, InvolvedPartiesCsiHubResponse, InvolvedPartiesCsiHubResponse> csiInvolvePartyGateway(
            JavaService<Request, InvolvedPartiesCsiHubResponse> csiInvolvePartyClient,
            InvolvedPartyCsiReqTransformer involvedPartyCsiReqTransformer,
            Validator validator,
            ExecutorService executorService
    ) {
        return new ClientGateway<>(csiInvolvePartyClient, involvedPartyCsiReqTransformer::transform, new ResponseValidator<InvolvedPartiesCsiHubResponse>(validator)::validate, executorService);
    }


}
