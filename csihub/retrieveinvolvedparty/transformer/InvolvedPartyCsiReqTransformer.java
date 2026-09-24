package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.CSIHubInvolvedPartiesRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvolvedPartyCsiReqTransformer extends RequestTransformer<CSIHubInvolvedPartiesRequest> {


    public InvolvedPartyCsiReqTransformer(@Value("${rest.csi-involved-party-retrieve-party-url}") @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(CSIHubInvolvedPartiesRequest input) {

        try {
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(String.format(getUrlFormat()))
                    .withJsonContent(input)
                    .build();

            log.info(C3LogMarker.marker, "CSI Hub API: Calling CSI Hub API endpoint[{}] with requestPayload {}", request.hashCode(), JsonUtils.getJsonFromObject(input));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Retrieve involve party details Data: Error when trying to create request payload with exception message {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }

    }
}
