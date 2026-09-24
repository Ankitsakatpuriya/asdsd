package com.ing.bankguarantees.service.dossier;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.database.CustomDocumentDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.CustomDocument;
import com.ing.bankguarantees.models.domain.DossierUpdateData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.CustomDocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.response.DossierUpdateResponse;
import com.ing.bankguarantees.models.response.DossierUploadDetailResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.AgreementDossierDataResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.ing.bankguarantees.utils.ConstantUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class DossierProcessingServiceTest {

    private static final String LEGAL_ENTITY_ID = "c135b9ae-d6ee-4898-b236-f33a400d09fd";
    private static final String REQUEST_DOSSIER_RESPONSE_ID = "12345678";
    private static final String AGREEMENT_DOSSIER_RESPONSE_ID = "87654321";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String REQUEST_ID = "07873b5c-489d-43cb-9cba-77f81905e682";
    private static final String REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4eb";
    private static final String SESSION_ID = java.util.UUID.randomUUID().toString();
    private final String CUSTOM_DOC_EXTENSION = "pdf";

    @Mock
    private CustomDocumentDao customDocumentDao;

    @Mock
    private DossierSetupService dossierSetupService;

    @InjectMocks
    private DossierProcessingService dossierProcessingService;


    @Test
    void getDossierDetailsPositive() {
        AccessToken accessToken = MockHelper.getAccessToken();
        AgreementDossierDataResponse agreementDossierResponse = MockHelper.getAgreementDossierResponse(REQUEST_DOSSIER_RESPONSE_ID, AGREEMENT_DOSSIER_RESPONSE_ID);
        given(dossierSetupService.setupDossier(any())).willReturn(CompletableFuture.completedFuture(agreementDossierResponse));
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(customDocumentDao.saveDocument(any())).willReturn(null);
        DossierUploadDetailResponse dossierUploadDetailResponse = dossierProcessingService.getDossierDetails(accessToken, BankGuaranteeCode.CUSTOMIZED_TEXT, LEGAL_ENTITY_ID).join();
        assertThat(dossierUploadDetailResponse).isNotNull();
        assertThat(dossierUploadDetailResponse.documentId()).isEqualTo(DOCUMENT_ID);
        assertThat(dossierUploadDetailResponse.applicationOwnerId()).isEqualTo(ING_APPLICATION_OWNER_ID);
        assertThat(dossierUploadDetailResponse.dossierType()).isEqualTo(REQUEST_DOSSIER_TYPE_CODE);
        assertThat(dossierUploadDetailResponse.dossierSubType()).isEqualTo(REQUEST_DOSSIER_SUBTYPE_CODE);
        assertThat(dossierUploadDetailResponse.documentType()).isEqualTo(CUSTOM_DOC_TYPE_CODE);
        assertThat(dossierUploadDetailResponse.documentSubType()).isEqualTo(CUSTOM_DOC_SUBTYPE_CODE);
        String docName = String.format("%s.%s", DocumentType.CUSTOMIZED_DOC.getDescription(), CUSTOM_DOC_EXTENSION);
        assertThat(dossierUploadDetailResponse.documentName()).isEqualTo(docName);
    }

    @Test
    void getDossierDetailsError() {
        AccessToken accessToken = MockHelper.getAccessToken();
        BgosException exception = assertThrows(BgosException.class, () -> dossierProcessingService.getDossierDetails(accessToken, BankGuaranteeCode.CUSTOM_2, LEGAL_ENTITY_ID));
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("BGOS-00-026");
    }

    @Test
    void updateDossierPositive() {
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CustomDocument customDocument = MockHelper.getCustomDocument(LEGAL_ENTITY_ID, REQUEST_DOSSIER_RESPONSE_ID,
                AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_ID, DOCUMENT_ID, REQUESTER_ID);
        DossierUpdateData dossierUpdateData = MockHelper.getDossierUpdateData(LEGAL_ENTITY_ID, REQUEST_ID, DOCUMENT_ID);
        given(customDocumentDao.getDocumentEntityByRequestId(any())).willReturn(CompletableFuture.completedFuture(customDocument));
        DossierUpdateResponse dossierUpdateResponse = dossierProcessingService.updateDossier(accessToken, dossierUpdateData).join();
        assertThat(dossierUpdateResponse).isNotNull();
        assertThat(dossierUpdateResponse.requestId()).isEqualTo(REQUEST_ID);
        assertThat(dossierUpdateResponse.status()).isEqualTo(CustomDocumentStatus.COMPLETED);
    }

    @Test
    void updateDossierInvalidRequesterPositive() {
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CustomDocument customDocument = MockHelper.getCustomDocument(LEGAL_ENTITY_ID, REQUEST_DOSSIER_RESPONSE_ID,
                AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_ID, DOCUMENT_ID, REQUEST_ID);
        DossierUpdateData dossierUpdateData = MockHelper.getDossierUpdateData(LEGAL_ENTITY_ID, REQUEST_ID, DOCUMENT_ID);
        given(customDocumentDao.getDocumentEntityByRequestId(any())).willReturn(CompletableFuture.completedFuture(customDocument));
        CompletableFuture<DossierUpdateResponse> future = dossierProcessingService.updateDossier(accessToken, dossierUpdateData);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-021");
    }

    @Test
    void updateDossierInvalidOrganisationPositive() {
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CustomDocument customDocument = MockHelper.getCustomDocument(LEGAL_ENTITY_ID, REQUEST_DOSSIER_RESPONSE_ID,
                AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_ID, DOCUMENT_ID, REQUESTER_ID);
        DossierUpdateData dossierUpdateData = MockHelper.getDossierUpdateData(REQUEST_ID, REQUEST_ID, DOCUMENT_ID);
        given(customDocumentDao.getDocumentEntityByRequestId(any())).willReturn(CompletableFuture.completedFuture(customDocument));
        CompletableFuture<DossierUpdateResponse> future = dossierProcessingService.updateDossier(accessToken, dossierUpdateData);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void updateDossierInvalidDocumentIdPositive() {
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        CustomDocument customDocument = MockHelper.getCustomDocument(LEGAL_ENTITY_ID, REQUEST_DOSSIER_RESPONSE_ID,
                AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_ID, DOCUMENT_ID, REQUESTER_ID);
        DossierUpdateData dossierUpdateData = MockHelper.getDossierUpdateData(LEGAL_ENTITY_ID, REQUEST_ID, AGREEMENT_DOSSIER_RESPONSE_ID);
        given(customDocumentDao.getDocumentEntityByRequestId(any())).willReturn(CompletableFuture.completedFuture(customDocument));
        CompletableFuture<DossierUpdateResponse> future = dossierProcessingService.updateDossier(accessToken, dossierUpdateData);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

}
