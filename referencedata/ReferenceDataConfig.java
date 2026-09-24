package com.ing.bankguarantees.remote.rest.referencedata;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeOutput;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer.ReferenceDataAttributeReqTransformer;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.transformer.ReferenceDataAttributeResTransformer;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer.ReferenceDataMultilingualReqTransformer;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer.ReferenceDataMultilingualResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.ExecutorService;


@Configuration
@Validated
public class ReferenceDataConfig {


    @Bean("referenceDataMultilingualRestClient")
    public JavaService<Request, ReferenceDataMultilingualResponse> referenceDataMultilingualRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            final RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                ReferenceDataMultilingualResponse.class,
                ErrorSource.RDA);
    }

    @Bean("referenceDataAttributeRestClient")
    public JavaService<Request, ReferenceDataAttributeResponse> referenceDataAttributeRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            final RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                ReferenceDataAttributeResponse.class,
                ErrorSource.RDA);
    }


    @Bean("referenceDataMultilingualClientGateway")
    public ClientGateway<Void, List<ReferenceDataMultilingualResponse.ReferenceData>, ReferenceDataMultilingualResponse> referenceDataMultilingualClientGateway(
             JavaService<Request, ReferenceDataMultilingualResponse> referenceDataMultilingualRestClient,
             ReferenceDataMultilingualReqTransformer requestTransformer,
             ReferenceDataMultilingualResTransformer responseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {
        return new ClientGateway<>(referenceDataMultilingualRestClient,
                requestTransformer::transform,
                (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<ReferenceDataMultilingualResponse>(validator).validate(res),
                executorService);
    }

    @Bean("referenceDataAttributeClientGateway")
    public ClientGateway<Void, ReferenceDataAttributeOutput, ReferenceDataAttributeResponse> referenceDataAttributeClientGateway(
             JavaService<Request, ReferenceDataAttributeResponse> referenceDataAttributeRestClient,
             ReferenceDataAttributeReqTransformer referenceDataAttributeReqTransformer,
             ReferenceDataAttributeResTransformer referenceDataAttributeResTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {
        return new ClientGateway<>(referenceDataAttributeRestClient,
                referenceDataAttributeReqTransformer::transform,
                (res, in) -> referenceDataAttributeResTransformer.transform(res),
                res -> new ResponseValidator<ReferenceDataAttributeResponse>(validator).validate(res),
                executorService);
    }

}
