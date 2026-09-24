package com.ing.bankguarantees.remote.rest.kfccreditoverview;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer.CustomerArrangementReqTransformer;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer.CustomerArrangementsRespTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.concurrent.ExecutorService;


@Validated
@Configuration
public class KFCCreditOverviewConfig {

    public JavaService<Request, CustomerArrangementResponse> getCustomerArrangementClient(Service<Request, Response> httpClient, RestServiceFactory restServiceFactory) {
        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                CustomerArrangementResponse.class,
                ErrorSource.CLAA);
    }

    @Bean("customerArrangementClientGateway")
    public ClientGateway<String, Optional<CustomerArrangementOutput>, CustomerArrangementResponse> customerArrangementClientGateway(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory,
            CustomerArrangementReqTransformer customerArrangementReqTransformer,
            CustomerArrangementsRespTransformer customerArrangementsRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(getCustomerArrangementClient(httpClient, restServiceFactory),
                customerArrangementReqTransformer::transform,
                (res, in) -> customerArrangementsRespTransformer.transform(res),
                new ResponseValidator<CustomerArrangementResponse>(validator)::validate,
                executorService);
    }
}
