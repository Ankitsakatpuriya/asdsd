package com.ing.bankguarantees.remote.rest.pamqualification.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.pamqualification.PamQualificationProperties;
import com.ing.bankguarantees.remote.rest.pamqualification.model.request.PamQualificationRequest;
import com.ing.bankguarantees.remote.rest.pamqualification.model.request.PamQualificationRequest.PartyRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;


@Slf4j
@Component
public class PamQualificationReqTransformer extends RequestTransformer<String> {

    private final PamQualificationProperties pamQualificationProperties;

    public PamQualificationReqTransformer(@Value("${rest.pam-qualification-check.parties-url}")
                                          String urlFormat, PamQualificationProperties properties) {
        super(urlFormat);
        this.pamQualificationProperties = properties;
    }

    @Override
    public Request transform(String id) {
        log.info("PamQualificationRequestTransformer [transform] call ");
        try {

            PamQualificationRequest requestPayload = getPamQualificationRequestPayload(id);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(requestPayload)
                    .build();
            log.info(C3LogMarker.marker, "PAMQualificationAPI: Calling POST {} endpoint[{}] with payload {}", getUrlFormat(),
                    request.hashCode(), getJsonFromObject(requestPayload));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.info("PamQualificationRequestTransformer [transform] Error when trying to create request payload with exception {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private PamQualificationRequest getPamQualificationRequestPayload(String beneficiaryId) {

        return PamQualificationRequest.builder()
                .beneficiary(PartyRequest.builder()
                        .id(beneficiaryId)
                        .type(pamQualificationProperties.getUuidCode())
                        .build())
                .ingEntity(pamQualificationProperties.getIngEntity())
                .operation(pamQualificationProperties.getOperation())
                .build();

    }


}
