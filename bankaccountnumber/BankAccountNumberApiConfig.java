package com.ing.bankguarantees.remote.rest.bankaccountnumber;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.BankAccountNumberReqTransformer;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.BankAccountNumberResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;

@Configuration
@Validated
public class BankAccountNumberApiConfig {


    @Bean("bankAccountNumberRestClient")
    public JavaService<Request, BankAccountNumberResponse> bankAccountNumberRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                BankAccountNumberResponse.class,
                ErrorSource.ABA);
    }


    @Bean("bankAccountNumberGateway")
    public ClientGateway<String, String, BankAccountNumberResponse> bankAccountNumberGateway(
            JavaService<Request, BankAccountNumberResponse> bankAccountNumberRestClient,
            BankAccountNumberReqTransformer bankAccountNumberReqTransformer,
            BankAccountNumberResTransformer bankAccountNumberRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(bankAccountNumberRestClient, bankAccountNumberReqTransformer::transform, bankAccountNumberRespTransformer::transform,
                new ResponseValidator<BankAccountNumberResponse>(validator)::validate, executorService);
    }
}
