package com.ing.bankguarantees.service.documents;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.database.ReportingDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.AgreementDossierDataResponse;
import com.ing.bankguarantees.service.dossier.DossierProcessingService;
import com.ing.bankguarantees.service.reporting.DataLakeReportingService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String REQUEST_DOSSIER_RESPONSE_ID = "12345678";
    private static final String AGREEMENT_DOSSIER_RESPONSE_ID = "87654321";
    private static final String BANK_ACCOUNT_NUMBER = "1102603141";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private DocumentDao documentDao;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @Mock
    private ReportingDao reportingDao;

    @Mock
    private DossierProcessingService dossierProcessingService;

    @Mock
    private DataLakeReportingService dataLakeReportingService;

    @Mock
    private ClientGateway<String, String, BankAccountNumberResponse> bankAccountNumberGateway;

    @InjectMocks
    private DocumentProcessingService documentProcessingService;


    private static Stream<Arguments> docGenerateStatus() {
        return Stream.of(Arguments.of(true, true),
                Arguments.of(false, false),
                Arguments.of(true, false),
                Arguments.of(false, true));
    }

    @ParameterizedTest
    @MethodSource("docGenerateStatus")
    void checkGenerateDocumentsAsynchronouslyPositive(Boolean conceptStatus, Boolean contractStatus) {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        AgreementDossierDataResponse agreementDossierResponse = MockHelper.getAgreementDossierResponse(REQUEST_DOSSIER_RESPONSE_ID, AGREEMENT_DOSSIER_RESPONSE_ID);
        given(bankAccountNumberGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(BANK_ACCOUNT_NUMBER));
        given(dossierProcessingService.fetchDossierDetails(any(), any())).willReturn(CompletableFuture.completedFuture(agreementDossierResponse));
        given(documentGeneratorService.generateContract(any(), any())).willReturn(CompletableFuture.completedFuture(contractStatus));
        given(documentGeneratorService.generateConcept(any(), any())).willReturn(CompletableFuture.completedFuture(conceptStatus));
        given(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).willReturn(bankGuaranteeRequest);
        given(dataLakeReportingService.sendDataLakeEvent(any())).willReturn(CompletableFuture.completedFuture(true));
        doNothing().when(reportingDao).updateStatusByRequestId(any(), any());

        AccessToken accessToken = MockHelper.getAccessToken();
        documentProcessingService.generateDocumentsAsynchronously(accessToken, bankGuaranteeRequest).join();
        verify(bankAccountNumberGateway, times(1)).performRequest(any());
        verify(dossierProcessingService, times(1)).fetchDossierDetails(any(), any());
        verify(documentGeneratorService, times(1)).generateContract(any(), any());
        verify(documentGeneratorService, times(1)).generateConcept(any(), any());
        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).updateStatusByRequestId(any(), any());

    }


    @Test
    void checkGenerateDocumentsAsynchronouslyError() {
        AccessToken accessToken = MockHelper.getAccessToken();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        given(bankAccountNumberGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> voidCompletableFuture = documentProcessingService.generateDocumentsAsynchronously(accessToken, bankGuaranteeRequest);
        CompletionException exception = assertThrows(CompletionException.class, voidCompletableFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(reportingDao, times(1)).updateStatusByRequestId(any(), any());
        verify(bankGuaranteeRequestDao, times(1)).updateStatusByRequestId(any(), any());
        verify(documentDao, times(1)).updateStatusByRequestId(any(), any());
    }


    @Test
    void checkSaveRequestInDatabasePositive() {

        AccessToken accessToken = MockHelper.getAccessToken();
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        BankGuaranteeRequest expected = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        expected.setMasterReferenceNumber(MASTER_REFERENCE_ID);
        expected.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        Reporting reporting = MockHelper.getReporting(REQUESTER_ID);

        given(documentDao.saveDocument(any())).willReturn(document);
        given(bankGuaranteeRequestDao.saveBankGuaranteeRequest(any())).willReturn(expected);
        given(reportingDao.saveReportingEntity(any())).willReturn(reporting);

        BankGuaranteeRequest actual = documentProcessingService.saveRequestInDatabase(accessToken, MASTER_REFERENCE_ID, bankGuaranteeRequestData);
        assertThat(actual).isNotNull();
        assertThat(actual.getIndividualId()).isEqualTo(UUID_INDV);
        assertThat(actual.getOrganisationId()).isEqualTo(UUID_ORG);
        assertThat(actual.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(actual.getCreatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(actual.getUpdatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(actual.getBgRequest()).isEqualTo(bankGuaranteeRequestData);
        assertThat(actual.getStatus()).isEqualTo(BankGuaranteeRequestStatus.DRAFT);
        assertThat(actual.getMasterReferenceNumber()).isEqualTo(MASTER_REFERENCE_ID);

        verify(bankGuaranteeRequestDao, times(1)).saveBankGuaranteeRequest(any());
        verify(reportingDao, times(1)).saveReportingEntity(any());
        verify(documentDao, times(2)).saveDocument(any());

    }


    @ParameterizedTest
    @MethodSource("docGenerateStatus")
    void checkGenerateFinalDocumentsPositive(Boolean conceptStatus, Boolean contractStatus) {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        given(documentGeneratorService.generateFinalBankGuarantee(any())).willReturn(CompletableFuture.completedFuture(conceptStatus));
        given(documentGeneratorService.generateFinalContractLetter(any())).willReturn(CompletableFuture.completedFuture(contractStatus));
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(documentDao.saveDocument(any())).willReturn(document);
        given(documentDao.saveDocument(any())).willReturn(document);

        Boolean result = documentProcessingService.generateFinalDocuments(bankGuaranteeRequest).join();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(conceptStatus && contractStatus);
        verify(documentGeneratorService, times(1)).generateFinalBankGuarantee(any());
        verify(documentGeneratorService, times(1)).generateFinalContractLetter(any());
        verify(documentDao, times(2)).saveDocument(any());

    }

}
