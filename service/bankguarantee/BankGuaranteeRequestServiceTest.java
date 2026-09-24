package com.ing.bankguarantees.service.bankguarantee;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.InvolvePartyData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.domain.StpResultDataSet.STPResultData;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.models.response.BankGuaranteeSubmitResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.masterapireference.model.MasterReferenceResponse;
import com.ing.bankguarantees.service.aler.AlerSignatoriesProcessingService;
import com.ing.bankguarantees.service.beneficiary.BeneficiaryDetailService;
import com.ing.bankguarantees.service.documents.DocumentProcessingService;
import com.ing.bankguarantees.service.documentsigning.SigningEligibilityService;
import com.ing.bankguarantees.service.dossier.DossierProcessingService;
import com.ing.bankguarantees.service.financial.FinancialDetailService;
import com.ing.bankguarantees.service.involveparty.InvolvePartyDetailsService;
import com.ing.bankguarantees.service.stp.StpRuleEvaluatorService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.util.TestConstants.BG_STP_RESULT_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeRequestServiceTest {


    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";

    @Mock
    private FinancialDetailService financialDetailService;

    @Mock
    private StpRuleEvaluatorService stpRuleEvaluatorService;

    @Mock
    private DocumentProcessingService documentProcessingService;

    @Mock
    private InvolvePartyDetailsService involvePartyDetailsService;

    @Mock
    private ClientGateway<Void, String, MasterReferenceResponse> masterReferenceClientGateway;

    @Mock
    private AlerSignatoriesProcessingService alerSignatoriesProcessingService;

    @Mock
    private SigningEligibilityService signingEligibilityService;

    @Mock
    private BeneficiaryDetailService beneficiaryDetailService;

    @Mock
    private DossierProcessingService dossierProcessingService;

    @Mock
    private ExecutorService executorService = ExecutorConfig.workStealingPool();

    @InjectMocks
    private BankGuaranteeRequestService bankGuaranteeRequestService;

    private static final String INVOLVE_PARTY_RESPONSE_FILE = "BGA/BGR/involve_party_data_response.json";

    private AccessToken accessToken;


    private StpResultDataSet stpResultDataset;

    private BankGuaranteeRequestData testRequestData;

    @BeforeEach
    void setUp() {
        accessToken = MockHelper.getAccessToken();
        String sessionId = accessToken.getClaimsSet().getSetId();
        testRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        testRequestData.setStpResultDataSet(stpResultDataset);
        String legalEntityId = testRequestData.getInstructingParty().getOrganisation().getLegalEntityId();
        String legalRepId = testRequestData.getInstructingParty().getIndividual().getLegalRepId();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(legalEntityId, legalRepId, sessionId, legalRepId);
        List<Identifier> organisationIdentifier = testRequestData.getInstructingParty().getOrganisation().getInternalIdentifiers();
        List<Identifier> individualIdentifier = testRequestData.getInstructingParty().getIndividual().getInternalIdentifiers();
        bankGuaranteeRequest.setBgRequest(testRequestData);
        lenient().when(involvePartyDetailsService.getInvolvePartyData(anyString())).thenReturn(CompletableFuture.completedFuture(MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE)));
        lenient().when(financialDetailService.validateFinancialInformation(testRequestData.getFinancialInformation(), legalEntityId)).thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(involvePartyDetailsService.getInternalIdentifierResponse(legalEntityId, false)).thenReturn(CompletableFuture.completedFuture(organisationIdentifier));
        lenient().when(involvePartyDetailsService.getInternalIdentifierResponse(legalRepId, true)).thenReturn(CompletableFuture.completedFuture(individualIdentifier));
        lenient().when(alerSignatoriesProcessingService.updateAssociatedSignatories(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(stpRuleEvaluatorService.performSTPChecks(any(), any())).thenReturn(CompletableFuture.completedFuture(stpResultDataset));
        lenient().when(documentProcessingService.saveRequestInDatabase(any(), any(), any())).thenReturn(bankGuaranteeRequest);
        lenient().when(documentProcessingService.generateDocumentsAsynchronously(accessToken, bankGuaranteeRequest)).thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(masterReferenceClientGateway.performRequest(null)).thenReturn(CompletableFuture.completedFuture(MASTER_REFERENCE_ID));
        lenient().when(signingEligibilityService.checkAmountCapRule(any())).thenReturn(CompletableFuture.completedFuture(false));
        lenient().when(dossierProcessingService.validateCustomDocumentDetails(any())).thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(dossierProcessingService.updateCustomDocDetails(any())).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void submitBankGuaranteePositive() {
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        BankGuaranteeSubmitResponse bankGuaranteeSubmitResponse = bankGuaranteeRequestService.submitBankGuarantee(accessToken, testRequestData).join();
        assertThat(bankGuaranteeSubmitResponse).isNotNull();
        assertThat(bankGuaranteeSubmitResponse.getRequestId()).isNotBlank();
        assertThat(bankGuaranteeSubmitResponse.isAllowSigning()).isTrue();
        assertThat(testRequestData.getInstructingParty().getOrganisation().getLegalForm()).isEqualTo(involvePartyDataResponse.getLegalForm());
    }


    @Test
    void submitBankGuaranteeMissingGuaranteePayload() {

        BankGuaranteeSubmitResponse bankGuaranteeSubmitResponse = bankGuaranteeRequestService.submitBankGuarantee(accessToken, testRequestData).join();
        assertThat(bankGuaranteeSubmitResponse).isNotNull();
        assertThat(bankGuaranteeSubmitResponse.getRequestId()).isNotBlank();
        assertThat(bankGuaranteeSubmitResponse.isAllowSigning()).isTrue();
    }

    @Test
    void submitBankGuaranteeCustomerSignedNotAllowed() {
        STPResultData stpResultData = stpResultDataset.getStpResultByType(StpCriteriaType.CUSTOMER_SIGN_ALLOWED).get();
        stpResultData.setStpPossible(false);
        BankGuaranteeSubmitResponse bankGuaranteeSubmitResponse = bankGuaranteeRequestService.submitBankGuarantee(accessToken, testRequestData).join();
        assertThat(bankGuaranteeSubmitResponse).isNotNull();
        assertThat(bankGuaranteeSubmitResponse.getRequestId()).isNotBlank();
        assertThat(bankGuaranteeSubmitResponse.isAllowSigning()).isFalse();
    }

    @Test
    void submitBankGuaranteeInvalidMasterReferenceNegative() {
        when(masterReferenceClientGateway.performRequest(null)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<BankGuaranteeSubmitResponse> future = bankGuaranteeRequestService.submitBankGuarantee(accessToken, testRequestData);
        CompletionException exception = Assertions.assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(financialDetailService, times(1)).validateFinancialInformation(testRequestData.getFinancialInformation(),
                testRequestData.getInstructingParty().getOrganisation().getLegalEntityId());
        verify(involvePartyDetailsService, times(1)).getInvolvePartyData(testRequestData.getInstructingParty().getOrganisation().getLegalEntityId());
        verify(involvePartyDetailsService, times(1)).getInternalIdentifierResponse(testRequestData.getInstructingParty().getIndividual().getLegalRepId(), true);
    }


}
