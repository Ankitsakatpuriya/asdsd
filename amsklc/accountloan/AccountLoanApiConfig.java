package com.ing.bankguarantees.remote.rest.amsklc.accountloan;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;

import com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer.AccountLoanReqTransformer;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer.AccountLoanResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Configuration
public class AccountLoanApiConfig {

    @Bean("accountLoanRestClient")
    public JavaService<Request, AccountLoanResponse> accountLoanRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                AccountLoanResponse.class,
                ErrorSource.AKB);
    }

    @Bean("acctLoanGateway")
    public ClientGateway<String, Optional<BigDecimal>, AccountLoanResponse> acctLoanGateway(
             JavaService<Request, AccountLoanResponse> accountLoanRestClient,
             AccountLoanReqTransformer accountLoanReqTransformer,
             AccountLoanResTransformer accountLoanResTransformer,
             Validator validator,
             @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(accountLoanRestClient, accountLoanReqTransformer::transform, accountLoanResTransformer::transform,
                new ResponseValidator<AccountLoanResponse>(validator)::validate, executorService);
    }
}
