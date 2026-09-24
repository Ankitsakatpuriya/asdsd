package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.AgreementDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.CreateAgreementDossierDataRequest;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Creates Request Payload from given parameter
 */
@Slf4j
@Component
public class AgreementDossierReqTransformer extends RequestTransformer<AgreementDossierDataInput> {

    private  final String AGREEMENT_DOSSIER_TYPE_CODE = "BLEBGAD01";
    private  final String AGG_DOSSIER_SUBTYPE_CODE = "BLEBGAS01";

    private  final String ONE_PAM_ID = "OnePAM";
    private  final String TITLE = "Agreement Dossier - BGOW";
    private  final String STATUS_NEW = "New";


    public AgreementDossierReqTransformer(@Value("${rest.gcc.case-management.agreement-dossiers.url}") @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform( AgreementDossierDataInput input) {
        try {

            CreateAgreementDossierDataRequest createAgreementDossierDataRequest = prepareAgreementDossierRequest(input);
             Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat().formatted(getUrlFormat()))
                    .withJsonContent(createAgreementDossierDataRequest)
                    .build();

            log.info(C3LogMarker.marker, "CommonCoreDataAPI[Create]: Calling POST {} endpoint[{}] to " +
                            "get AgreementId for the Agreement Request {}", getUrlFormat(), request.hashCode(),
                    JsonUtils.getJsonFromObject(createAgreementDossierDataRequest));
            return request;

        } catch (RichHttpRequestBuilderException ex) {
            log.error("CommonCoreAPI[Create]: Error when trying to create request payload for reference {} " +
                    "with exception message {}", getUrlFormat(), ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    private CreateAgreementDossierDataRequest prepareAgreementDossierRequest(AgreementDossierDataInput input) {
        return new CreateAgreementDossierDataRequest()
                .toBuilder()
                .title(TITLE)
                .ingPartyType(Set.of(ONE_PAM_ID))
                .ingReferenceId(Set.of(input.getLegalEntityId()))
                .ingDosSubstatus(IngDos.builder().code(STATUS_NEW).build())
                .ingDosGroupId(input.getReqResponseId())
                .ingDosType(IngDos.builder().code(AGREEMENT_DOSSIER_TYPE_CODE).build())
                .ingDosSubtype(IngDos.builder().code(AGG_DOSSIER_SUBTYPE_CODE).build())
                .build();
    }


}
