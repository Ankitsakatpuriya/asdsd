package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer;

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
public class ExtIdentReqTransformer extends RequestTransformer<String> {

    public ExtIdentReqTransformer(@Value("${rest.involved-party-external-identifiers-url}")
                                   String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform( String uuid) {

        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(String.format(getUrlFormat()))
                .withParam("uuid", uuid)
                .build();
        log.info("Calling  GET {} endpoint[{}] for individual uuid {}", getUrlFormat(), request.hashCode(),
                uuid);
        return request;

    }

}