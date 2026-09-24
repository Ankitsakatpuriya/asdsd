package com.ing.bankguarantees.remote.rest.accountbalance;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
import com.ing.bankguarantees.remote.rest.accountbalance.transformer.AccountBalanceReqTransformer;
import com.ing.bankguarantees.remote.rest.accountbalance.transformer.AccountBalanceResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Configuration
public class AccountBalanceApiConfig {

    @Bean("accountBalanceRestClient")
    public JavaService<Request, AccountBalanceResponse> accountBalanceRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                AccountBalanceResponse.class,
                ErrorSource.ABA);
    }

    @Bean("acctBalGateway")
    public ClientGateway<AccountBalanceInput, List<AccountBalanceOutput>, AccountBalanceResponse> acctBalanceGateway(
             JavaService<Request, AccountBalanceResponse> accountBalanceRestClient,
             AccountBalanceReqTransformer acctBalReqTransformer,
             AccountBalanceResTransformer acctBalRespTransformer,
             Validator validator,
             @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(accountBalanceRestClient, acctBalReqTransformer::transform, acctBalRespTransformer::transform,
                new ResponseValidator<AccountBalanceResponse>(validator)::validate, executorService);
    }
}
