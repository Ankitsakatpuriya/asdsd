package com.ing.bankguarantees.remote.rest.intake.transformers;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.intake.IntakeApiProperties;
import com.ing.bankguarantees.remote.rest.intake.mapper.IntakeApiRequestMapper;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;
import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;

@Slf4j
@Component
public class IntakeApiRequestTransformer extends RequestTransformer<IntakeApiInput> {

    private static final String ING_CHANNEL = "X-ING-Channel";
    private final IntakeApiRequestMapper intakeApiRequestMapper;
    private final IntakeApiProperties intakeApiProperties;


    public IntakeApiRequestTransformer(@Value("${rest.active-bank-guarantees.intake.url}") String urlFormat,
                                       IntakeApiRequestMapper intakeApiRequestMapper,
                                       IntakeApiProperties intakeApiProperties) {
        super(urlFormat);
        this.intakeApiRequestMapper = intakeApiRequestMapper;
        this.intakeApiProperties = intakeApiProperties;
    }

    @Override
    public Request transform(IntakeApiInput intakeApiInput) {

        try {
            IntakeApiRequest intakeApiRequest = intakeApiRequestMapper.prepareIntakeApiRequest(intakeApiInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withHeader(ING_CHANNEL, intakeApiProperties.getIngChannel())
                    .withJsonContent(intakeApiRequest)
                    .build();
            log.info(C3LogMarker.marker, "Active Bank Guarantees API: Calling POST {} endpoint[{}] with the request payload {}", getUrlFormat(),
                    request.hashCode(), getJsonFromObject(intakeApiRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call intake API {}", ex.getMessage());
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }
}