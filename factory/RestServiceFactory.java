package com.ing.bankguarantees.remote.factory;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.api.ServiceConverter;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.apisdk.toolkit.connectivity.filter.ResponseTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorDataMapper;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.HttpStatusFilter;
import com.ing.bankguarantees.remote.common.PdfResponseMapperFilter;
import com.ing.bankguarantees.remote.common.ResponseMapperFilter;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Class to provide java service for our application
 */
@Component
@Validated
@AllArgsConstructor
public class RestServiceFactory {

    private final ObjectMapper mapper;
    private final Map<ErrorSource, ErrorDataMapper> errorDataMapperMap;

    /**
     * Creates a {@link JavaService} capable ok making REST calls by chaining {@code requestFilter}, {@link
     * HttpStatusFilter}, {@link ResponseMapperFilter} and {@code httpClient}
     *
     * @param httpClient    client used to make the REST calls
     * @param requestFilter filter that builds transforms {@code Req} to {@link Request}
     * @param responseType  expected {@link Class} of the response
     * @param <Req>         request {@link Class}
     * @param <Res>         response {@link Class}
     * @param errorSource   source system {@link ErrorSource}
     * @return {@link JavaService} capable of making REST calls
     */
    public <Req, Res> JavaService<Req, Res> getRestService(
            @NotNull Service<Request, Response> httpClient,
            @NotNull RequestTransformingFilter<Req, Res, Request> requestFilter,
            @NotNull Class<Res> responseType,
            @NotNull ErrorSource errorSource) {

        // Map response
        return getService(requestFilter, new ResponseMapperFilter<>(responseType, mapper), errorSource, httpClient);
    }

    public <Req, Res> JavaService<Req, Res> getPdfService(
            @NotNull Service<Request, Response> httpClient,
            @NotNull RequestTransformingFilter<Req, Res, Request> requestFilter,
            @NotNull Class<Res> responseType,
            @NotNull ErrorSource errorSource) {

        // Map response
        PdfResponseMapperFilter<Res> responseMapperFilter = new PdfResponseMapperFilter<>(responseType);
        return getService(requestFilter, responseMapperFilter, errorSource, httpClient);
    }


    private <Req, Res> JavaService<Req, Res> getService(@NotNull RequestTransformingFilter<Req, Res, Request> requestFilter,
                                                        @NotNull ResponseTransformingFilter<Request, Res, Response> responseMapperFilter,
                                                        @NotNull ErrorSource errorSource,
                                                        @NotNull Service<Request, Response> httpClient) {
        // Check http status
        HttpStatusFilter httpStatusFilter = new HttpStatusFilter(errorDataMapperMap.get(errorSource));
        // Service
        Service<Req, Res> requestService =
                requestFilter
                        .andThen(responseMapperFilter)
                        .andThen(httpStatusFilter)
                        .andThen(httpClient);
        // To Java
        return ServiceConverter.asJava(requestService);
    }

}