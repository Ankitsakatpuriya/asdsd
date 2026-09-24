package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.CreateDocumentPlaceholderDataRequest;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DocumentPlaceHolderIn;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.ing.bankguarantees.utils.ConstantUtils.*;


@Slf4j
@Component
public class PlaceholderDossierReqTransformer extends RequestTransformer<DocumentPlaceHolderIn> {


    private static final String CATEGORY = "Financial document";
    private static final String STATUS = "Proposal";
    private static final String DOSSIER_PARAM = "dossier_id";


    public PlaceholderDossierReqTransformer(@Value("${rest.gcc.case-management.place-holder.documents.url}") @NotNull String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(DocumentPlaceHolderIn input) {

        try {

            CreateDocumentPlaceholderDataRequest createDocumentPlaceholderDataRequest = preparePlaceholderRequest(input);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withParam(DOSSIER_PARAM, input.getDossierId())
                    .withUrl(getUrlFormat().formatted(getUrlFormat()))
                    .withJsonContent(createDocumentPlaceholderDataRequest)
                    .build();

            log.info(C3LogMarker.marker, "CommonCoreDataAPI[Create]: Calling POST {} endpoint[{}] and to get DocumentPlaceholderId " +
                            "for the DocumentPlaceholder Request {}", getUrlFormat(), request.hashCode(),
                    JsonUtils.getJsonFromObject(createDocumentPlaceholderDataRequest));
            return request;

        } catch (RichHttpRequestBuilderException ex) {
            log.error("CommonCoreAPI[Create]: Error when trying to create request payload for reference {} " +
                    "with exception message {}", getUrlFormat(), ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    private CreateDocumentPlaceholderDataRequest preparePlaceholderRequest(DocumentPlaceHolderIn input) {
        return new CreateDocumentPlaceholderDataRequest()
                .toBuilder()
                .name(input.getDocumentType().getDescription())
                .title(input.getDocumentType().getDescription())
                .ingLanguageCode(Set.of(input.getLocale().getLanguage()))
                .ingIfwCategory(CATEGORY)
                .ingDocStatus(IngDos.builder().code(STATUS).build())
                .ingDocType(IngDos.builder().code(getDocumentTypeCode(input.getDocumentType())).build())
                .ingDocSubtype(IngDos.builder().code(getDocumentSubTypeCode(input.getDocumentType())).build())
                .build();
    }

    private String getDocumentSubTypeCode(DocumentType documentType) {

        return switch (documentType) {
            case BG_DRAFT -> BANK_GUARANTEE_DRAFT_DOCUMENT_SUBTYPE_CODE;
            case CONTRACT -> CONTRACT_DRAFT_DOCUMENT_SUBTYPE_CODE;
            case BG_FINAL -> BANK_GUARANTEE_FINAL_DOCUMENT_SUBTYPE_CODE;
            case CONTRACT_FINAL -> CONTRACT_FINAL_DOCUMENT_SUBTYPE_CODE;
            case CUSTOMIZED_DOC -> CUSTOM_DOC_SUBTYPE_CODE;
        };
    }

    private String getDocumentTypeCode(DocumentType documentType) {
        return switch (documentType) {
            case BG_DRAFT -> BANK_GUARANTEE_DRAFT_DOCUMENT_TYPE_CODE;
            case CONTRACT -> CONTRACT_DRAFT_DOCUMENT_TYPE_CODE;
            case BG_FINAL -> BANK_GUARANTEE_FINAL_DOCUMENT_TYPE_CODE;
            case CONTRACT_FINAL -> CONTRACT_FINAL_DOCUMENT_TYPE_CODE;
            case CUSTOMIZED_DOC -> CUSTOM_DOC_TYPE_CODE;
        };
    }


}
