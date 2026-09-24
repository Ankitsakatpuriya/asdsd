package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.CreateRequestDossierDataRequest;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.RequestDossierDataInput;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ing.bankguarantees.utils.ConstantUtils.REQUEST_DOSSIER_SUBTYPE_CODE;
import static com.ing.bankguarantees.utils.ConstantUtils.REQUEST_DOSSIER_TYPE_CODE;


/**
 * Creates Request Payload from given parameter
 */
@Slf4j
@Component
public class RequestDossierReqTransformer extends RequestTransformer<RequestDossierDataInput> {



    private static final String STATUS_NEW = "New";
    private static final String BE_ENTITY = "BE";
    private static final String ONE_PAM_ID = "OnePAM";


    public RequestDossierReqTransformer(@Value("${rest.gcc.case-management.request-dossiers.url}") @NotNull String urlFormat) {
        super(urlFormat);
    }


    @Override
    public Request transform(RequestDossierDataInput input) {
        try {

            CreateRequestDossierDataRequest requestDossierRequestPayload = prepareRequestDossierPayload(input);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(requestDossierRequestPayload)
                    .build();

            log.info(C3LogMarker.marker, "CommonCoreDataAPI[Create]: Calling POST {} endpoint[{}] and" +
                            " to get RequestId for the  RequestDossier Request {} ", getUrlFormat(), request.hashCode(),
                    JsonUtils.getJsonFromObject(requestDossierRequestPayload));

            return request;

        } catch (RichHttpRequestBuilderException ex) {
            log.error("CommonCoreAPI[Create]: Error when trying to create request payload for reference {} " +
                    "with exception message {}", getUrlFormat(), ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private CreateRequestDossierDataRequest prepareRequestDossierPayload(RequestDossierDataInput input) {

        return new CreateRequestDossierDataRequest()
                .toBuilder()
                .ingPartyType(List.of(ONE_PAM_ID))
                .ingReferenceId(List.of(input.getLegalEntityId()))
                .ingDosSubstatus(IngDos.builder().code(STATUS_NEW).build())
                .caseId(input.getRequestId())
                .reportingEntity(BE_ENTITY)
                .ingDosType(IngDos.builder().code(REQUEST_DOSSIER_TYPE_CODE).build())
                .ingDosSubtype(IngDos.builder().code(REQUEST_DOSSIER_SUBTYPE_CODE).build())
                .build();
    }


}
