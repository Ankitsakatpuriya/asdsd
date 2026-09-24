package com.ing.bankguarantees.remote.rest.holidaycalendar.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HolidayCalendarRequestTransformer extends RequestTransformer<HolidayCalendarRequest> {


    private static final String CENTER_BRB = "BrB";
    private static final String PARAM_NAME_CENTER = "center";
    private static final String QRY_PARAM_FROM = "from";
    private static final String QRY_PARAM_TO = "to";
    private static final String QRY_PARAM_WEEKENDS = "weekends";

    protected HolidayCalendarRequestTransformer(@Value("${rest.reference-data.attribute.holidays-url}") String urlFormat) {
        super(urlFormat);
    }


    @Override
    public Request transform(HolidayCalendarRequest input) {

        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(getUrlFormat())
                .withParam(PARAM_NAME_CENTER, CENTER_BRB)
                .withQueryParam(QRY_PARAM_FROM, input.getFromDate().toString())
                .addQueryParam(QRY_PARAM_TO, input.getToDate().toString())
                .addQueryParam(QRY_PARAM_WEEKENDS, "true")
                .build();

        log.info(C3LogMarker.marker, "Holiday Calendar API : Calling GET {} endpoint [{}] with params {}",
                getUrlFormat(), request.hashCode(), input);
        return request;

    }

}