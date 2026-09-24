package com.ing.bankguarantees.remote.rest.ccaas.getdocument;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer.DocumentRequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer.DocumentResponseTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;

import java.util.concurrent.ExecutorService;


@Slf4j
@Configuration
public class DocumentConfig {


    @Bean("documentRestClient")
    public JavaService<Request, byte[]> documentRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {
        return restServiceFactory.getPdfService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                byte[].class,
                ErrorSource.CCA);
    }


    @Bean("requestDocumentClientGateway")
    public ClientGateway<String, ByteArrayResource, byte[]> requestDocumentClientGateway(
            JavaService<Request, byte[]> documentRestClient,
            DocumentRequestTransformer requestTransformer,
            DocumentResponseTransformer responseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(documentRestClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<byte[]>(validator).validate(res), executorService);
    }


}
