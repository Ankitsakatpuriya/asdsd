package com.ing.bankguarantees.remote.rest.involveparty.grantees;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.GranteeGrantorResponse;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.transformer.InvPartyGranteeReqTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;


/**
 * Configuration class for {@link JavaService JavaServices} that call  API
 */
@Configuration
@Validated
@RequiredArgsConstructor
public class InvPartyGranteeApiConfig {

    @Bean("invPartyGranteeRestClient")
    public JavaService<Request, GranteeGrantorResponse> invPartyGranteeRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                GranteeGrantorResponse.class,
                ErrorSource.IPA);
    }

    @Bean("invPartyGranteeGateway")
    public ClientGateway<String, GranteeGrantorResponse, GranteeGrantorResponse> invPartyGranteeGateway(
            JavaService<Request, GranteeGrantorResponse> invPartyGranteeRestClient,
            InvPartyGranteeReqTransformer invPartyGranteeReqTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(invPartyGranteeRestClient, invPartyGranteeReqTransformer::transform, new ResponseValidator<GranteeGrantorResponse>(validator)::validate, executorService);
    }
}
