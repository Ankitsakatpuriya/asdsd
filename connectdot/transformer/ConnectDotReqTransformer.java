package com.ing.bankguarantees.remote.rest.connectdot.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotMapper;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class ConnectDotReqTransformer extends RequestTransformer<ConnectDotInput> {

    private final ConnectDotMapper connectDotMapper;

    public ConnectDotReqTransformer(@Value("${rest.message.send.with-response.url}") @NotNull String urlFormat,
                                    ConnectDotMapper connectDotMapper) {
        super(urlFormat);
        this.connectDotMapper = connectDotMapper;
    }

    @Override
    public Request transform( ConnectDotInput connectDotInput) {

        try {
            ConnectDotRequest<?> connectDotRequest = connectDotMapper.prepareConnectDotRequest(connectDotInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(connectDotRequest)
                    .build();

            log.info(C3LogMarker.marker, "Calling ConnectDotAPI endpoint[{}] with request payload {}", request.hashCode(),
                    JsonUtils.getJsonFromObject(connectDotRequest));

            return request;
        } catch ( RichHttpRequestBuilderException ex) {
            log.error(" ConnectDotAPI: Error when trying to create request payload for reference {}  with" +
                    " exception message {}", connectDotInput, ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR,ex);
        }

    }
}
