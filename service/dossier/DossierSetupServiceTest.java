package com.ing.bankguarantees.service.dossier;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.DossierRequestData;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.AgreementDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DocumentPlaceHolderIn;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.RequestDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.AgreementDossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.DossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentInput;
import com.ing.bankguarantees.service.dossier.DossierSetupService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class DossierSetupServiceTest {

    private static final String LEGAL_ENTITY_ID = "c135b9ae-d6ee-4898-b236-f33a400d09fd";
    private static final String REQUEST_ID = "07873b5c-489d-43cb-9cba-77f81905e682";
    private static final String REQUEST_DOSSIER_RESPONSE_ID = "12345678";
    private static final String AGREEMENT_DOSSIER_RESPONSE_ID = "87654321";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String UPDATE_AGREEMENT_DOSSIER_RESPONSE_ID = "344135tplot";
    private static final String UPDATE_REQUEST_DOSSIER_RESPONSE_ID = "344135tplov";

    @Mock
    private ClientGateway<RequestDossierDataInput, String, DossierDataResponse> requestDossierClientGateway;

    @Mock
    private ClientGateway<DocumentPlaceHolderIn, String, DossierDataResponse> placeholderDossierClientGateway;

    @Mock
    private ClientGateway<AgreementDossierDataInput, AgreementDossierDataResponse, AgreementDossierDataResponse> agreementDossierClientGateway;

    @Mock
    private ClientGateway<UploadDocumentInput, String, DossierDataResponse> uploadDocumentClientGateway;

    @Mock
    private ClientGateway<UpdateDossierDataInput, String, DossierDataResponse> updateAgreementDossierDataGateway;

    private DossierSetupService dossierSetupService;


    @BeforeEach
    void setUp() {
        dossierSetupService = new DossierSetupService(requestDossierClientGateway, placeholderDossierClientGateway,
                updateAgreementDossierDataGateway, uploadDocumentClientGateway, agreementDossierClientGateway);
    }

    @Test
    void checkSetupDossierPositive() {
        AccessToken accessToken = MockHelper.getAccessToken();
        DossierRequestData dossierInput = MockHelper.getDossierInput(REQUEST_ID, LEGAL_ENTITY_ID, accessToken);
        AgreementDossierDataResponse agreementDossierResponse = MockHelper.getAgreementDossierResponse(REQUEST_DOSSIER_RESPONSE_ID, AGREEMENT_DOSSIER_RESPONSE_ID);
        given(requestDossierClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(REQUEST_DOSSIER_RESPONSE_ID));
        given(agreementDossierClientGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(agreementDossierResponse));
        AgreementDossierDataResponse agreementDossierDataResponse = dossierSetupService.setupDossier(dossierInput).join();
        assertThat(agreementDossierDataResponse).isNotNull();
        assertThat(agreementDossierDataResponse).isEqualTo(agreementDossierResponse);
    }

    @Test
    void checkSetupDossierRequestDossierResponseNull() {
        AccessToken accessToken = MockHelper.getAccessToken();
        DossierRequestData dossierInput = MockHelper.getDossierInput(REQUEST_ID, LEGAL_ENTITY_ID, accessToken);
        given(requestDossierClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<AgreementDossierDataResponse> agreementDossierFuture = dossierSetupService.setupDossier(dossierInput);
        CompletionException exception = assertThrows(CompletionException.class, agreementDossierFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkSetupDossierAgreementDossierResponseNull() {
        AccessToken accessToken = MockHelper.getAccessToken();
        DossierRequestData dossierInput = MockHelper.getDossierInput(REQUEST_ID, LEGAL_ENTITY_ID, accessToken);
        given(requestDossierClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(REQUEST_DOSSIER_RESPONSE_ID));
        given(agreementDossierClientGateway.performRequestWithValidate(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<AgreementDossierDataResponse> agreementDossierFuture = dossierSetupService.setupDossier(dossierInput);
        CompletionException exception = assertThrows(CompletionException.class, agreementDossierFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkSetupPlaceholderPositive() {
        given(placeholderDossierClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        String actualDocumentId = dossierSetupService.setupPlaceHolder(AGREEMENT_DOSSIER_RESPONSE_ID, DocumentType.BG_DRAFT, Locale.UK).join();
        assertThat(actualDocumentId).isNotBlank();
        assertThat(actualDocumentId).isEqualTo(DOCUMENT_ID);
    }

    @Test
    void checkSetupPlaceholderResponseNull() {
        given(placeholderDossierClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<String> placeHolderFuture = dossierSetupService.setupPlaceHolder(AGREEMENT_DOSSIER_RESPONSE_ID, DocumentType.BG_DRAFT, Locale.UK);
        CompletionException exception = assertThrows(CompletionException.class, placeHolderFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkCloseCaseInDocumentumPositive() {
        given(updateAgreementDossierDataGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(UPDATE_AGREEMENT_DOSSIER_RESPONSE_ID));
        given(updateAgreementDossierDataGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(UPDATE_REQUEST_DOSSIER_RESPONSE_ID));
        String actualResponse = dossierSetupService.closeCaseInDocumentum(AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_DOSSIER_RESPONSE_ID).join();
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(UPDATE_REQUEST_DOSSIER_RESPONSE_ID);
    }

    @Test
    void checkCloseCaseInDocumentumUpdateDossierNull() {
        given(updateAgreementDossierDataGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<String> closeCaseFuture = dossierSetupService.closeCaseInDocumentum(AGREEMENT_DOSSIER_RESPONSE_ID, REQUEST_DOSSIER_RESPONSE_ID);
        CompletionException exception = assertThrows(CompletionException.class, closeCaseFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkCloseCaseRequestDossierPositive() {
        given(updateAgreementDossierDataGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(UPDATE_REQUEST_DOSSIER_RESPONSE_ID));
        String actualResponse = dossierSetupService.closeCaseRequestDossier(REQUEST_DOSSIER_RESPONSE_ID).join();
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(UPDATE_REQUEST_DOSSIER_RESPONSE_ID);
    }

    @Test
    void checkCloseCaseRequestDossierError() {
        given(updateAgreementDossierDataGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<String> updateDossirFuture = dossierSetupService.closeCaseRequestDossier(REQUEST_DOSSIER_RESPONSE_ID);
        CompletionException exception = assertThrows(CompletionException.class, updateDossirFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkUploadDocumentPositive() {
        given(uploadDocumentClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        UploadDocumentInput uploadDocumentInput = MockHelper.getUploadDocumentInput(DOCUMENT_ID, DocumentType.BG_FINAL);
        String actualDocumentId = dossierSetupService.uploadDocument(uploadDocumentInput).join();
        assertThat(actualDocumentId).isNotBlank();
        assertThat(actualDocumentId).isEqualTo(DOCUMENT_ID);
    }

    @Test
    void checkUploadDocumentResponseNull() {
        given(uploadDocumentClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        UploadDocumentInput uploadDocumentInput = MockHelper.getUploadDocumentInput(DOCUMENT_ID, DocumentType.BG_FINAL);
        CompletableFuture<String> uploadedDocumentFuture = dossierSetupService.uploadDocument(uploadDocumentInput);
        CompletionException exception = assertThrows(CompletionException.class, uploadedDocumentFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

}
