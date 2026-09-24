package com.ing.bankguarantees.remote.rest.ccaas.dossier.update.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateRequestAgreementDossierDataRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.ConstantUtils.ING_APPLICATION_OWNER_ID;


@Slf4j
@Component
public class UpdateRequestDossierRequestTransformer extends RequestTransformer<UpdateDossierDataInput> {

    private static final String DOSSIER_TYPE = "dossierType";
    private static final String DOSSIER_ID = "dossierId";

    public UpdateRequestDossierRequestTransformer(@Value("${rest.common-core-api.update-dossier-path}") @NotNull  String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(UpdateDossierDataInput updateDossierDataInput) {
        try {
             UpdateRequestAgreementDossierDataRequest requestPayload = getRequestPayload(updateDossierDataInput);
             Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Put())
                    .withParam(DOSSIER_TYPE, updateDossierDataInput.getDossierType().getDocType())
                    .withParam(DOSSIER_ID, updateDossierDataInput.getDossierId())
                    .withUrl(getUrlFormat())
                    .withJsonContent(requestPayload)
                    .build();
            log.info(C3LogMarker.marker, "CommonCoreDataAPI[Update]: Calling POST {} endpoint[{}] to " +
                            "update request dossier with payload {}", getUrlFormat(), request.hashCode(),
                    JsonUtils.getJsonFromObject(requestPayload));

            return request;
        } catch (RichHttpRequestBuilderException exception) {
            log.error("UpdateAgreementDossierRequestTransformer [transform] Error while creating request to call " +
                    "update request dossier API  {}", exception.getMessage());
            throw new BgosException(ErrorCode.INVALID_REQUEST, exception);
        }
    }

    private static UpdateRequestAgreementDossierDataRequest getRequestPayload( UpdateDossierDataInput dossierDataIN) {
        return UpdateRequestAgreementDossierDataRequest.builder()
                .ingApplicationOwnerId(ING_APPLICATION_OWNER_ID)
                .ingDosSubstatus(new IngDos(dossierDataIN.getStatus()))
                .build();

    }


}
