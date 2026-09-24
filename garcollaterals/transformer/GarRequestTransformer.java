package com.ing.bankguarantees.remote.rest.garcollaterals.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class GarRequestTransformer extends RequestTransformer<GarCollateralsInput> {


    private static final String HEADER_RCID = "X-ING-RCID";
    private static final String HEADER_SOURCE = "X-ING-SOURCE";
    private final String appName;

    protected GarRequestTransformer(@Value("${rest.gar-collaterals.url}") @NotNull String urlFormat,
                                    @Value("${bgos.service-name}") @NotNull String applicationName) {
        super(urlFormat);
        appName = applicationName;
    }

    @Override
    public Request transform(GarCollateralsInput input) {
        try {

            GarRequest garRequest = prepareGarRequest(input);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withHeader(HEADER_RCID, "BE-BG")
                    .withHeader(HEADER_SOURCE, appName)
                    .withJsonContent(garRequest)
                    .build();

            log.info(C3LogMarker.marker, "GarAPI : Calling POST {} endpoint[{}] with request payload {}", getUrlFormat(),
                    request.hashCode(),
                    JsonUtils.getJsonFromObject(garRequest));

            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("GarAPI: Error when trying to create request payload for reference {} with " +
                    "exception message {}", input, ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    public GarRequest prepareGarRequest(GarCollateralsInput input) {
        return GarRequest.builder()
                .language(input.getLocale().getLanguage().toUpperCase())
                .involvedPartyIdentifiers(List.of(input.getIdentifier()))
                .build();
    }
}
