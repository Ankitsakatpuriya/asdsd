package com.ing.bankguarantees.remote.rest.permission;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.remote.rest.permission.model.response.PermissionResponse;
import com.ing.bankguarantees.remote.rest.permission.transformer.PermissionRequestTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@Configuration
public class PermissionConfig {

    @Bean("permissionRestClient")
    public JavaService<Request, PermissionResponse> permissionRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                PermissionResponse.class,
                ErrorSource.PMS);
    }

    @Bean("permissionGateway")
    public ClientGateway<PermissionRequest, PermissionResponse, PermissionResponse> permissionGateway(
            JavaService<Request, PermissionResponse> permissionRestClient,
            PermissionRequestTransformer permissionRequestTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(permissionRestClient, permissionRequestTransformer::transform, new ResponseValidator<PermissionResponse>(validator)::validate, executorService);
    }

}
