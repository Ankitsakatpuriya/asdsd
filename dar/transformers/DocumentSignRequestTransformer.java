package com.ing.bankguarantees.remote.rest.dar.transformers;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.dar.mapper.DarRequestMapper;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;
import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;

@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class DocumentSignRequestTransformer extends RequestTransformer<DarInput> {

    private final DarRequestMapper requestMapper;


    protected DocumentSignRequestTransformer(@Value("${rest.api-dar-document-sign.url}") String urlFormat,
                                             DarRequestMapper requestMapper) {
        super(urlFormat);
        this.requestMapper = requestMapper;
    }

    @Override
    public Request transform(DarInput darInput) {

        try {
            DarRequest darRequest = requestMapper.prepareDarRequest(darInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(String.format(getUrlFormat()))
                    .withJsonContent(darRequest)
                    .withHeader("Host", getUrlFormat())
                    .build();
            log.info(C3LogMarker.marker, "DocSign Orchestrator API: Calling POST {} endpoint[{}] with the request payload {}", getUrlFormat(),
                    request.hashCode(), getJsonFromObject(darRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }
}