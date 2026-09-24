package com.ing.bankguarantees.remote.rest.aler.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;

@Slf4j
@Component
public class AlerRetrieveSignatoriesRequestTransformer extends RequestTransformer<AlerRetrieveSignatoriesInput> {

    private static final String PURPOSE_ID = "1013";

    public AlerRetrieveSignatoriesRequestTransformer(@Value("${rest.representation-management-be.retrieve-signatories.url}")
                                                     @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(AlerRetrieveSignatoriesInput input) {
        try {
            AlerRetrieveSignatoriesRequest retrieveSignatoriesPayload = createAlerRetrieveSignatoriesPayload(input);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withHeader(AccessToken.HEADER_NAME, input.accessToken().serialize())
                    .addQueryParam("transactionId", UUID.randomUUID().toString())
                    .addQueryParam("purposeId", PURPOSE_ID)
                    .addQueryParam("language", input.translation().getLanguage())
                    .withJsonContent(retrieveSignatoriesPayload)
                    .build();

            log.info(C3LogMarker.marker, """
                    Representation Management BE API: Calling POST {} endpoint[{}] for organisationId {} to retrieve \
                    active signatories, with payload {}\
                    """, getUrlFormat(), request.hashCode(), input.organisationId(), getJsonFromObject(retrieveSignatoriesPayload));

            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call aler api {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private AlerRetrieveSignatoriesRequest createAlerRetrieveSignatoriesPayload(AlerRetrieveSignatoriesInput alerRetrieveSignatoriesInput) {
        return AlerRetrieveSignatoriesRequest.builder()
                .organisationIdentifier(alerRetrieveSignatoriesInput.organisationId())
                .onlyActiveSignatories(false)
                .build();
    }
}
