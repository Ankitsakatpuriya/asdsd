package com.ing.bankguarantees.remote.rest.productagreement;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.rest.productagreement.transformer.ProductAgreReqTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.transformer.ProductAgreeRespTransformer;
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
public class ProductAgreeApiConfig {

    @Bean("productAgreeRestClient")
    public JavaService<Request, ProductAgreementResponse> productAgreeRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                ProductAgreementResponse.class,
                ErrorSource.PAA);
    }

    @Bean("productAgreAccountClientGateway")
    public ClientGateway<String, List<ProductAgreementAccount>, ProductAgreementResponse> productAgreAccountClientGateway(
            JavaService<Request, ProductAgreementResponse> productAgreeRestClient,
            ProductAgreReqTransformer productAgreReqTransformer,
            ProductAgreeRespTransformer productAgreRespTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(productAgreeRestClient, productAgreReqTransformer::transform, productAgreRespTransformer::transform,
                new ResponseValidator<ProductAgreementResponse>(validator)::validate, executorService);
    }
}
