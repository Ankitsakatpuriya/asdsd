package com.ing.bankguarantees.service.documents;

import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.domain.StpResultDataSet.STPResultData;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.response.ConnectDotResponse;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarResponse;
import com.ing.bankguarantees.service.dossier.DossierSetupService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static com.ing.bankguarantees.utils.ConstantUtils.CSI;
import static com.ing.bankguarantees.utils.ConstantUtils.CSI_BE_SOLE_PRPTRP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
class DocumentGeneratorServiceTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String AGREEMENT_DOSSIER_RESPONSE_ID = "87654321";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private DossierSetupService dossierSetupService;

    @Mock
    private DocumentDao documentDao;

    @Mock
    private ClientGateway<ConnectDotInput, Boolean, ConnectDotResponse> connectDotApiClientGateway;

    @Mock
    private ClientGateway<GarCollateralsInput, Optional<GarOutput>, GarResponse> garCollateralsGateway;

    private DocumentGeneratorService documentGeneratorService;
    private BankGuaranteeRequestData bankGuaranteeRequestData;
    private StpResultDataSet stpResultDataSet;
    private BankGuaranteeRequest bankGuaranteeRequest;

    @BeforeEach
    void setup() {
        documentGeneratorService = new DocumentGeneratorService(dossierSetupService, documentDao,
                connectDotApiClientGateway, garCollateralsGateway);
        stpResultDataSet = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataSet);
        bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
    }

    @Test
    void checkGenerateContractPositive() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkGenerateContractGenerateDocumentApiEmptyResponse() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isFalse();

    }

    @Test
    void checkGenerateContractGenerateDocumentApiNegativeResponse() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(false));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isFalse();

    }

    @Test
    void checkGenerateContractResolveCollateralPositive() {

        GarOutput garOutput = GarOutput.builder().build();
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        given(garCollateralsGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(Optional.of(garOutput)));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkGenerateContractResolveCollateralError() {

        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(garCollateralsGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        CompletableFuture<Boolean> booleanCompletableFuture = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest);
        CompletionException exception = assertThrows(CompletionException.class, booleanCompletableFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

    }

    @Test
    void checkGenerateContractResolveSDResponseRed() {

        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        stpResultDataSet = bankGuaranteeRequestData.getStpResultDataSet();
        STPResultData stpResultData = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        stpResultData.setStpPossible(false);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkGenerateContractResolveSDResponseMissing() {

        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        stpResultDataSet = bankGuaranteeRequestData.getStpResultDataSet();
        STPResultData stpResultData = stpResultDataSet.getStpResultByType(StpCriteriaType.SDS_RESPONSE).get();
        stpResultDataSet.getStpResults().remove(stpResultData);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        Boolean status = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkGenerateConceptCsiIdentifierMissing() {
        List<Identifier> internalIdentifiers = bankGuaranteeRequestData.getInstructingParty().getOrganisation().getInternalIdentifiers();
        Optional<Identifier> identifierValue = CommonUtils.getIdentifierValue(internalIdentifiers, List.of(CSI, CSI_BE_SOLE_PRPTRP_ID));
        internalIdentifiers.remove(identifierValue.get());
        bankGuaranteeRequestData.getFinancialInformation().setCreditLine(null);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);

        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        CompletableFuture<Boolean> booleanCompletableFuture = documentGeneratorService.generateContract(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest);
        CompletionException exception = assertThrows(CompletionException.class, booleanCompletableFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");

    }


    @Test
    void checkGenerateConceptPositive() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        lenient().when(dossierSetupService.setupPlaceHolder(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        lenient().when(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).thenReturn(CompletableFuture.completedFuture(document));
        lenient().when(connectDotApiClientGateway.performRequest(any())).thenReturn(CompletableFuture.completedFuture(true));
        Boolean status = documentGeneratorService.generateConcept(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkGenerateFinalContractPositive() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        Boolean status = documentGeneratorService.generateFinalContractLetter(bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isTrue();

    }

    @Test
    void checkFinalGenerateContractGenerateDocumentApiEmptyResponse() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        Boolean status = documentGeneratorService.generateFinalContractLetter(bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isFalse();

    }

    @Test
    void checkGenerateFinalContractGenerateDocumentApiNegativeResponse() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(false));
        Boolean status = documentGeneratorService.generateFinalContractLetter(bankGuaranteeRequest).join();
        assertThat(status).isNotNull();
        assertThat(status).isFalse();

    }

    @Test
    void checkGenerateFinalBankGuaranteePositive() {

        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        given(dossierSetupService.setupPlaceHolder(any(), any(), any())).willReturn(CompletableFuture.completedFuture(DOCUMENT_ID));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(connectDotApiClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(true));
        Boolean status = documentGeneratorService.generateFinalBankGuarantee(bankGuaranteeRequest).join();
        assertThat(status).isNotNull().isTrue();

    }

    @Test
    void checkGenerateConceptForGenerationNotAllow() {

        bankGuaranteeRequestData.getGuaranteeDetails().setBgCode(BankGuaranteeCode.CUSTOMIZED_TEXT);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Boolean status = documentGeneratorService.generateConcept(AGREEMENT_DOSSIER_RESPONSE_ID, bankGuaranteeRequest).join();
        assertThat(status).isNotNull().isTrue();

    }

    @Test
    void checkGenerateConceptForFinalGenerationNotAllow() {

        bankGuaranteeRequestData.getGuaranteeDetails().setBgCode(BankGuaranteeCode.CUSTOMIZED_TEXT);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Boolean status = documentGeneratorService.generateFinalBankGuarantee(bankGuaranteeRequest).join();
        assertThat(status).isNotNull().isTrue();
    }
}
