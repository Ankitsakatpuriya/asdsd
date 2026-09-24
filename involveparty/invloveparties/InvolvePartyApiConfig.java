package com.ing.bankguarantees.remote.rest.involveparty.invloveparties;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.OrganisationOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyIndividualResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyInternalIdentifierResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyOrgResTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer.InvPartyReqTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Configuration class for {@link JavaService JavaServices} that call  API
 */
@Configuration
@Validated
@RequiredArgsConstructor
public class InvolvePartyApiConfig {


    @Bean("invPartyOnePamRestClient")
    public JavaService<Request, InvolvedPartyOnePamResponse> invPartyOnePamRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                InvolvedPartyOnePamResponse.class,
                ErrorSource.IPA);
    }


    @Bean("involvePartyOrgClientGateway")
    public ClientGateway<InvolvePartyRequest, OrganisationOnePamResponse, InvolvedPartyOnePamResponse> involvePartyOrgClientGateway(
            JavaService<Request, InvolvedPartyOnePamResponse> invPartyOnePamRestClient,
            InvPartyReqTransformer invPartyReqTransformer,
            InvPartyOrgResTransformer invPartyOrgResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(invPartyOnePamRestClient, invPartyReqTransformer::transform, (res, in) -> invPartyOrgResTransformer.transform(res),
                new ResponseValidator<InvolvedPartyOnePamResponse>(validator)::validate, executorService);
    }

    @Bean("involvePartyIndividualClientGateway")
    public ClientGateway<InvolvePartyRequest, IndividualOnePamResponse, InvolvedPartyOnePamResponse> involvePartyIndividualClientGateway(
            JavaService<Request, InvolvedPartyOnePamResponse> invPartyOnePamRestClient,
            InvPartyReqTransformer invPartyReqTransformer,
            InvPartyIndividualResTransformer invPartyIndividualResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(invPartyOnePamRestClient, invPartyReqTransformer::transform, (res, in) -> invPartyIndividualResTransformer.transform(res),
                new ResponseValidator<InvolvedPartyOnePamResponse>(validator)::validate, executorService);
    }

    @Bean("involvePartyInternalIdentifierClientGateway")
    public ClientGateway<InvolvePartyRequest, List<Identifier>, InvolvedPartyOnePamResponse> involvePartyInternalIdentifierClientGateway(
            JavaService<Request, InvolvedPartyOnePamResponse> invPartyOnePamRestClient,
            InvPartyReqTransformer invPartyReqTransformer,
            InvPartyInternalIdentifierResTransformer invPartyInternalIdentifierResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(invPartyOnePamRestClient, invPartyReqTransformer::transform, invPartyInternalIdentifierResTransformer::transform,
                new ResponseValidator<InvolvedPartyOnePamResponse>(validator)::validate, executorService);
    }

}
