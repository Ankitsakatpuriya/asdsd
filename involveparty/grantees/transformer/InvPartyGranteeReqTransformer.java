package com.ing.bankguarantees.remote.rest.involveparty.grantees.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Requester transformer to transform the request
 */

@Slf4j
@Component
public class InvPartyGranteeReqTransformer extends RequestTransformer<String> {


    private static final String UUID = "uuid";
    private static final String OFFSET_TYPE = "offset";
    private static final String OFFSET_VALUE = "0";
    private static final String LIMIT_TYPE = "limit";
    private static final String LIMIT_VALUE = "1000";

    public InvPartyGranteeReqTransformer(@Value("${rest.involved-party-grantees-details-url}") String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(String uuid) {

        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(String.format(getUrlFormat()))
                .withParam(UUID, uuid)
                .addQueryParam(OFFSET_TYPE, OFFSET_VALUE)
                .addQueryParam(LIMIT_TYPE, LIMIT_VALUE)
                .build();
        log.info("Calling  GET {} endpoint[{}] for InvolvedPartyGranteeDetailRequestTransformer individual uuid {}", getUrlFormat(), request.hashCode(),
                uuid);
        return request;

    }

}