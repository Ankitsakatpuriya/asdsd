package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
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
public class InvPartyReqTransformer extends RequestTransformer<InvolvePartyRequest> {

    public InvPartyReqTransformer(@Value("${rest.involved-party-details-url}")  String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform( InvolvePartyRequest involvePartyRequest) {

        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(String.format(getUrlFormat()))
                .withParam("uuid", involvePartyRequest.getUuid())
                .build();
        log.info("Calling  GET {} endpoint[{}] for individual involvePartyRequest {}", getUrlFormat(), request.hashCode(),
                involvePartyRequest);
        return request;

    }

}