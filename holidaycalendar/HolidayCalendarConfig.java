package com.ing.bankguarantees.remote.rest.holidaycalendar;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.BankHoliday;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarRequest;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarResponse;
import com.ing.bankguarantees.remote.rest.holidaycalendar.transformer.HolidayCalendarRequestTransformer;
import com.ing.bankguarantees.remote.rest.holidaycalendar.transformer.HolidayCalendarResponseTransformer;
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
public class HolidayCalendarConfig {

    private JavaService<Request, HolidayCalendarResponse> holidayRestClient(Service<Request, Response> httpClient, RestServiceFactory restServiceFactory) {
        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                HolidayCalendarResponse.class,
                ErrorSource.HCA);
    }

    @Bean("holidayCalendarGateway")
    public ClientGateway<HolidayCalendarRequest, List<BankHoliday>, HolidayCalendarResponse> holidayCalendarGateway(
            @Qualifier("routingResilientHttpClient") Service<Request, Response> httpClient,
            RestServiceFactory restServiceFactory,
            HolidayCalendarRequestTransformer requestTransformer,
            HolidayCalendarResponseTransformer responseTransformer,
            Validator validator,
            @Qualifier("workStealingPool") ExecutorService executorService) {


        return new ClientGateway<>(holidayRestClient(httpClient, restServiceFactory), requestTransformer::transform,
                (res, in) -> responseTransformer.transform(res),
                new ResponseValidator<HolidayCalendarResponse>(validator)::validate, executorService);
    }

}
