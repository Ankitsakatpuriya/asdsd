package com.ing.bankguarantees.remote.rest.amsklc.booking;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingRequest;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingResponse;
import com.ing.bankguarantees.remote.rest.amsklc.booking.transformer.BookingReqTransformer;
import com.ing.bankguarantees.remote.rest.amsklc.booking.transformer.BookingResTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;


@Configuration
public class BookingApiConfig {


    @Bean("bookingRestClient")
    public JavaService<Request, BookingResponse> bookingRestClient(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                BookingResponse.class,
                ErrorSource.AKB);
    }

    @Bean("bookingClientGateway")
    public ClientGateway<BookingRequest, String, BookingResponse> bookingClientGateway(
            JavaService<Request, BookingResponse> bookingRestClient,
            BookingReqTransformer bookingLoanReqTransformer,
            BookingResTransformer bookingLoanResTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService
    ) {
        return new ClientGateway<>(bookingRestClient, bookingLoanReqTransformer::transform, (res, in) -> bookingLoanResTransformer.transform(res),
                new ResponseValidator<BookingResponse>(validator)::validate, executorService);
    }
}
