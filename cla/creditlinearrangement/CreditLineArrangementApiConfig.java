package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementRequest;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.response.CreditLineArrangementOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer.CreditLineArrangementReqTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer.CreditLineArrangementResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;


@Configuration
public class CreditLineArrangementApiConfig {

    @Bean("creditLineArrangementRestClient")
    public JavaService<Request, CreditLineArrangementOutput> creditLineArrangementRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                CreditLineArrangementOutput.class,
                ErrorSource.CLAA);
    }

    @Bean("creditLineArrangementClientGateway")
    public ClientGateway<CreditLineArrangementRequest, String, CreditLineArrangementOutput> creditLineArrangementClientGateway(
            JavaService<Request, CreditLineArrangementOutput> creditLineArrangementRestClient,
            CreditLineArrangementReqTransformer creditLineArrangementReqTransformer,
            CreditLineArrangementResTransformer creditLineArrangementResTransformer,
            Validator validator,
            @Qualifier("workStealingPool")ExecutorService executorService
    ) {
        return new ClientGateway<>(creditLineArrangementRestClient, creditLineArrangementReqTransformer::transform,
                (res, in) -> creditLineArrangementResTransformer.transform(res),
                new ResponseValidator<CreditLineArrangementOutput>(validator)::validate, executorService);
    }
}
