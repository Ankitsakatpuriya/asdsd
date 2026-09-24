package com.ing.bankguarantees.remote.rest.namescreening.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.namescreening.NameScreeningProperties;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningInput;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningRequest;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningRequest.Address;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningRequest.Party;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;

@Slf4j
@Component
public class NameScreeningReqTransformer extends RequestTransformer<NameScreeningInput> {

    private final NameScreeningProperties nameScreeningProperties;

    public NameScreeningReqTransformer(@Value("${rest.name-screening-detail-url}") String urlFormat, NameScreeningProperties nameScreeningProperties) {
        super(urlFormat);
        this.nameScreeningProperties = nameScreeningProperties;
    }

    @Override
    public Request transform(NameScreeningInput input) {
        log.info("NameScreeningReqTransformer [transform]  call ");
        try {
            NameScreeningRequest nameScreeningRequest = prepareNameScreeningRequest(input);
            Request request = new RichHttpRequestBuilder()
                    .withUrl(String.format(getUrlFormat()))
                    .withMethod(Method.Post())
                    .withJsonContent(nameScreeningRequest)
                    .build();
            log.info("NameScreeningAPI : Calling POST {} endpoint[{}] for {}", getUrlFormat(), request.hashCode(), getJsonFromObject(nameScreeningRequest));
            return request;
        } catch (RichHttpRequestBuilderException exception) {
            log.error("Error while parsing the request to call NameScreening api ");
            throw new BgosException(ErrorCode.INVALID_REQUEST, exception);
        }
    }

    private NameScreeningRequest prepareNameScreeningRequest(NameScreeningInput input) {
        String tsNow = ZonedDateTime.now(ZoneId.of("Europe/Brussels")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        return NameScreeningRequest.builder()
                .messageId(UUID.randomUUID().toString())
                .timestamp(tsNow)
                .businessUnit(nameScreeningProperties.getBusinessUnit())
                .actorCountry(nameScreeningProperties.getActorCountry())
                .screeningType(nameScreeningProperties.getScreeningType())
                .alertGenerate(nameScreeningProperties.getAlertGenerate())
                .party(prepareNameScreeningParty(input))
                .build();
    }

    private Party prepareNameScreeningParty(NameScreeningInput input) {
        return Party.builder().partyType(input.getPartyType())
                .organizationName(input.getOrganizationName())
                .fullName(input.getFullName())
                .birthDate(input.getDateOfBirth() != null ? input.getDateOfBirth().toString() : null)
                .address(Address.builder()
                        .city(input.getCity())
                        .street(input.getStreet())
                        .zipCode(input.getZipCode())
                        .countryCode(input.getCountryCode())
                        .houseNumber(input.getHouseNumber())
                        .build())
                .build();
    }
}
