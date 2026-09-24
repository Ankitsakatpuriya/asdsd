package com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.transform;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.IngDos;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.ing.bankguarantees.utils.ConstantUtils.*;


@Slf4j
@Component
public class UploadDocumentRequestTransformer extends RequestTransformer<UploadDocumentInput> {

    private static final String CATEGORY = "Financial document";
    private static final String STATUS = "Proposal";
    private static final String DOCUMENT_PARAM = "documentId";
    private static final String METADATA_PARAM = "meta-data";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "multipart/form-data";
    private static final String CONTENT = "content";

    public UploadDocumentRequestTransformer(@Value("${rest.gcc.case-management.upload.documents.url}") @NotNull String urlFormat) {
        super(urlFormat);
    }


    @Override
    public Request transform(UploadDocumentInput uploadDocumentInput) {
        try {
            UploadDocumentRequest uploadDocRequest = prepareUploadDocRequest(uploadDocumentInput);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
                    .withParam(DOCUMENT_PARAM, uploadDocumentInput.documentId())
                    .withUrl(String.format(getUrlFormat()))
                    .withJsonElement(METADATA_PARAM, uploadDocRequest)
                    .withFileElement(CONTENT, uploadDocumentInput.documentType().getDescription() + ".pdf",
                            uploadDocumentInput.fileContent().getByteArray(), MediaType.APPLICATION_PDF_VALUE)
                    .build();
            log.info(C3LogMarker.marker, "CommonCoreDataAPI[Upload new version]: Calling POST {} endpoint[{}] and to" +
                    "Upload document in Case management with request : {}", getUrlFormat(), request.hashCode(), JsonUtils.getJsonFromObject(uploadDocRequest));
            return request;
        } catch (Exception ex) {
            log.error("Error while creating request to call update document dossier API  {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private UploadDocumentRequest prepareUploadDocRequest(UploadDocumentInput uploadDocumentInput) {
        return UploadDocumentRequest.builder()
                .name(uploadDocumentInput.documentType().getDescription())
                .ingLanguageCode(Set.of(uploadDocumentInput.language()))
                .ingIfwCategory(CATEGORY)
                .ingDocStatus(IngDos.builder().code(STATUS).build())
                .ingDocType(IngDos.builder().code(getDocumentTypeCode(uploadDocumentInput.documentType())).build())
                .ingDocSubtype(IngDos.builder().code(getDocumentSubTypeCode(uploadDocumentInput.documentType())).build())
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
