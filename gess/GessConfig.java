package com.ing.bankguarantees.remote.rest.gess;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignInput;
import com.ing.bankguarantees.remote.rest.gess.model.response.GessSignResponse;
import com.ing.bankguarantees.remote.rest.gess.transformer.GessSignRequestTransformer;
import com.ing.bankguarantees.remote.rest.gess.transformer.GessSignResponseTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;

import java.util.concurrent.ExecutorService;


@Configuration
public class GessConfig {

    @Bean("gessSignRestClient")
    public JavaService<Request, GessSignResponse> gessSignRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                GessSignResponse.class,
                ErrorSource.GESS);
    }

    @Bean("gessSignGateway")
    public ClientGateway<GessSignInput, ByteArrayResource, GessSignResponse> gessSignGateway(
            JavaService<Request, GessSignResponse> gessSignRestClient,
            GessSignRequestTransformer gessSignRequestTransformer,
            GessSignResponseTransformer gessSignResponseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(gessSignRestClient, gessSignRequestTransformer::transform, (res, in) -> gessSignResponseTransformer.transform(res),
                new ResponseValidator<GessSignResponse>(validator)::validate, executorService);
    }
}
