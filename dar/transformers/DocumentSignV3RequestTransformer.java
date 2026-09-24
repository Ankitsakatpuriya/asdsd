package com.ing.bankguarantees.remote.rest.dar.transformers;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.dar.mapper.DarV3RequestMapper;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarV3Request;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;
import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;

@Slf4j
@Component
public class DocumentSignV3RequestTransformer extends RequestTransformer<DarInput> {

    private final DarV3RequestMapper requestMapper;


    protected DocumentSignV3RequestTransformer(@Value("${rest.api-dar-document-v3-sign.url}") String urlFormat,
                                               DarV3RequestMapper requestMapper) {
        super(urlFormat);
        this.requestMapper = requestMapper;
    }

    @Override
    public Request transform(DarInput darInput) {

        try {
            DarV3Request darV3Request = requestMapper.prepareDarV3Request(darInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(String.format(getUrlFormat()))
                    .withJsonContent(darV3Request)
                    .withHeader("Host", getUrlFormat())
                    .build();
            log.info(C3LogMarker.marker, "DocSign Orchestrator API: Calling POST {} endpoint[{}] with the request payload {}", getUrlFormat(),
                    request.hashCode(), getJsonFromObject(darV3Request));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call Doc sign Api {}", ex.getMessage());
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }
}