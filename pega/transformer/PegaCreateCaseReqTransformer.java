package com.ing.bankguarantees.remote.rest.pega.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.pega.mapper.PegaRequestMapper;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;


@Slf4j
@Component
public class PegaCreateCaseReqTransformer extends RequestTransformer<PegaCreateCaseInput> {

    private final PegaRequestMapper pegaRequestMapper;


    public PegaCreateCaseReqTransformer(@Value("${rest.pega.create-case-url}") String urlFormat,
                                        PegaRequestMapper pegaRequestMapper) {
        super(urlFormat);
        this.pegaRequestMapper = pegaRequestMapper;
    }

    @Override
    public Request transform(PegaCreateCaseInput pegaCreateCaseInput) {
        try {
            var createCaseRequest = pegaRequestMapper.preparePegaRequest(pegaCreateCaseInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(createCaseRequest)
                    .build();
            log.info(C3LogMarker.marker, "Pega Create Case : Calling POST {} endpoint[{}] with payload {}", getUrlFormat(),
                    request.hashCode(), getJsonFromObject(createCaseRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error(" Error when trying to create request payload with exception {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }
}
