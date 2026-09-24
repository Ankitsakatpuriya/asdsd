package com.ing.bankguarantees.service.aler;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.response.SignatorySelectionResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse;
import com.ing.bankguarantees.service.documentsigning.SigningEligibilityService;
import com.ing.bankguarantees.service.involveparty.InvolvePartyDetailsService;
import com.ing.bankguarantees.service.involveparty.InvolvePartyGranteeService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class AlerSignatoriesProcessingServiceTest {

    private static final String ALER_SIGNATORY_RESPONSE = "BGA/SSA/aler_signatories_data.json";
    private static final String INVOLVE_PARTY_RESPONSE_FILE = "BGA/BGR/involve_party_data_response.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4eb";
    private static final String SECOND_SIGNATORY_ID = "d8f80f8d-1b90-4ad7-b8c5-3b4d22e8c864";
    private static final String PASSIVE_SIGNATORY_ID = "d8f80f8d-1b90-4ad7-b8c5-3b4d22e8c889";
    private static final String INVALID_REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4ak34";
    private static final String UUID_ORG = "3f97bf90-9dd9-4acb-b99a-d4c49be71ece";
    private static final String TRANSACTION_ID = "3f97bf90-9dd9-4acb-b99a-d4c49be71eded";
    private static final String FAILED_TRANSACTION_STATUS = "FAILED";
    private static final String SUCCESS_TRANSACTION_STATUS = "SUCCESS";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String LEGAL_FORM_SOLE_PROPRIETOR = "BE_SOLE_PRPTRP";

    @Mock
    private InvolvePartyDetailsService involvePartyDetailsService;

    @Mock
    private InvolvePartyGranteeService involvePartyGranteeService;

    @Mock
    private SigningEligibilityService signingEligibilityService;

    @Mock
    private ClientGateway<AlerRetrieveSignatoriesInput, AlerSignatories, AlerRetrieveSignatoriesResponse> retrieveSignatoriesClientGateway;

    @InjectMocks
    private AlerSignatoriesProcessingService alerSignatoriesProcessingService;


    @BeforeEach
    void setUp() {

    }

    @Test
    void checkInitiateSignatoriesPositive() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        given(involvePartyDetailsService.getInvolvePartyData(anyString())).willReturn(CompletableFuture.completedFuture(involvePartyDataResponse));
        given(signingEligibilityService.checkAlerEligibility(alerSignatories, involvePartyDataResponse.isWbCustomer(), guaranteeDetails)).willReturn(CompletableFuture.completedFuture(false));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(alerSignatories));
        SignatoriesSelectionRequest signatoriesSelectionRequest = MockHelper.getSignatorySelectionRequest(UUID_ORG, guaranteeDetails);
        SignatorySelectionResponse signatorySelectionResponse = alerSignatoriesProcessingService.initiateSignatoriesSelection(accessToken, Locale.ENGLISH, signatoriesSelectionRequest).join();
        assertThat(signatorySelectionResponse).isNotNull();
        assertThat(signatorySelectionResponse.getTransactionId()).isNotBlank();
        assertThat(signatorySelectionResponse.isInvokeAler()).isTrue();
    }

    //
    @Test
    void checkInitiateSignatoriesNegative() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        given(involvePartyDetailsService.getInvolvePartyData(anyString())).willReturn(CompletableFuture.completedFuture(involvePartyDataResponse));
        given(signingEligibilityService.checkAlerEligibility(alerSignatories, involvePartyDataResponse.isWbCustomer(), guaranteeDetails)).willReturn(CompletableFuture.completedFuture(true));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(alerSignatories));

        SignatoriesSelectionRequest signatoriesSelectionRequest = MockHelper.getSignatorySelectionRequest(UUID_ORG, guaranteeDetails);
        SignatorySelectionResponse signatorySelectionResponse = alerSignatoriesProcessingService.initiateSignatoriesSelection(accessToken, Locale.ENGLISH, signatoriesSelectionRequest).join();
        assertThat(signatorySelectionResponse).isNotNull();
        assertThat(signatorySelectionResponse.getTransactionId()).isNotBlank();
        assertThat(signatorySelectionResponse.isInvokeAler()).isFalse();
    }

    @Test
    void checkInitiateSignatoriesUnauthorizedRequester() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        AccessToken accessToken = MockHelper.getAccessToken(INVALID_REQUESTER_ID, SESSION_ID, INVALID_REQUESTER_ID);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);

        given(involvePartyDetailsService.getInvolvePartyData(anyString())).willReturn(CompletableFuture.completedFuture(involvePartyDataResponse));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(alerSignatories));

        SignatoriesSelectionRequest signatoriesSelectionRequest = MockHelper.getSignatorySelectionRequest(UUID_ORG, guaranteeDetails);
        CompletableFuture<SignatorySelectionResponse> future = alerSignatoriesProcessingService.initiateSignatoriesSelection(accessToken, Locale.ENGLISH, signatoriesSelectionRequest);
        CompletionException exception = Assertions.assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-025");
    }


    @Test
    void checkInitiateSignatoriesRetrieveEmptyResponseError() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        given(involvePartyDetailsService.getInvolvePartyData(anyString())).willReturn(CompletableFuture.completedFuture(involvePartyDataResponse));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        SignatoriesSelectionRequest signatoriesSelectionRequest = MockHelper.getSignatorySelectionRequest(UUID_ORG, guaranteeDetails);
        CompletableFuture<SignatorySelectionResponse> future = alerSignatoriesProcessingService.initiateSignatoriesSelection(accessToken, Locale.ENGLISH, signatoriesSelectionRequest);
        CompletionException exception = Assertions.assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void checkInitiateSignatoriesSoleProprietor() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        InvolvePartyData involvePartyDataResponse = MockHelper.createInvolvePartyDataResponse(INVOLVE_PARTY_RESPONSE_FILE);
        involvePartyDataResponse.setLegalForm(LEGAL_FORM_SOLE_PROPRIETOR);
        given(involvePartyDetailsService.getInvolvePartyData(anyString())).willReturn(CompletableFuture.completedFuture(involvePartyDataResponse));
        SignatoriesSelectionRequest signatoriesSelectionRequest = MockHelper.getSignatorySelectionRequest(UUID_ORG, guaranteeDetails);
        SignatorySelectionResponse signatorySelectionResponse = alerSignatoriesProcessingService.initiateSignatoriesSelection(accessToken, Locale.ENGLISH, signatoriesSelectionRequest).join();
        assertThat(signatorySelectionResponse).isNotNull();
        assertThat(signatorySelectionResponse.getTransactionId()).isBlank();
        assertThat(signatorySelectionResponse.isInvokeAler()).isFalse();
    }


    @Test
    void checkUpdateAssociatedSignatoriesPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        IndividualOnePamResponse individualOnePamResponse = MockHelper.getIndividualEmailDigitalAddressOnePamResponse();
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        alerSignatories.signatories().add(AlerSignatories.Signatory.builder().signatoryUUID(PASSIVE_SIGNATORY_ID).isActive(false).signingPower(2).build());
        AlerSignatories alerSignatoriesResponse = MockHelper.getAlerSignatories(alerSignatories.transactionId(), alerSignatories.transactionStatus(), alerSignatories.signatories(), null);
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString())).willReturn(CompletableFuture.completedFuture(new ArrayList<>(List.of(REQUESTER_ID, SECOND_SIGNATORY_ID))));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(alerSignatoriesResponse));
        given(signingEligibilityService.getSigners(any(), any())).willReturn(bankGuaranteeRequestData.getAlerDetails().getSigners());
        try (MockedStatic<CommonUtils> mockStatic = Mockito.mockStatic(CommonUtils.class)) {
            mockStatic.when(() -> CommonUtils.retrieveOnePamIdentifier(any(), any())).thenReturn(REQUESTER_ID, SECOND_SIGNATORY_ID, PASSIVE_SIGNATORY_ID);
            Void rvoid = alerSignatoriesProcessingService.updateAssociatedSignatories(accessToken, bankGuaranteeRequestData).join();
            LegalRepresentativeData signerLr = getSignerUUID(bankGuaranteeRequestData.getLegalRepresentatives());
            assertThat(bankGuaranteeRequestData.getAlerDetails()).isNotNull();
            assertThat(signerLr.isSigner()).isTrue();
            assertThat(signerLr.isFirstSigner()).isTrue();
            assertThat(signerLr.getUuid()).contains(bankGuaranteeRequestData.getAlerDetails().getSigners());
            assertThat(bankGuaranteeRequestData.getAlerDetails().getTransactionId()).isNotNull();
            assertThat(bankGuaranteeRequestData.getAlerDetails().getTransactionStatus()).isEqualTo(SUCCESS_TRANSACTION_STATUS);
        }
    }

    @Test
    void checkForEmptySignatoriesList() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        IndividualOnePamResponse individualOnePamResponse = MockHelper.getIndividualEmailDigitalAddressOnePamResponse();
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        AlerSignatories errorSignatories = MockHelper.getAlerSignatories(alerSignatories.transactionId(), alerSignatories.transactionStatus(),
                Collections.emptyList(), alerSignatories.errorDetails());
        bankGuaranteeRequestData.getAlerDetails().setSigners(Collections.emptyList());
        bankGuaranteeRequestData.getAlerDetails().setInvokeAler(false);

        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString())).willReturn(CompletableFuture.completedFuture(new ArrayList<>(List.of(REQUESTER_ID, SECOND_SIGNATORY_ID))));
        given(retrieveSignatoriesClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(errorSignatories));
        lenient().when(signingEligibilityService.totalLegalRep(any())).thenReturn(2L);
        try (MockedStatic<CommonUtils> mockStatic = Mockito.mockStatic(CommonUtils.class)) {
            mockStatic.when(() -> CommonUtils.retrieveOnePamIdentifier(any(), any())).thenReturn(REQUESTER_ID, SECOND_SIGNATORY_ID);
            Void rvoid = alerSignatoriesProcessingService.updateAssociatedSignatories(accessToken, bankGuaranteeRequestData).join();
            LegalRepresentativeData signerLr = getSignerUUID(bankGuaranteeRequestData.getLegalRepresentatives());
            assertThat(bankGuaranteeRequestData.getAlerDetails()).isNotNull();
            assertThat(signerLr).isNull();
            assertThat(CollectionUtils.size(bankGuaranteeRequestData.getLegalRepresentatives())).isEqualTo(2);
            assertThat(bankGuaranteeRequestData.getLegalRepresentatives().stream().filter(LegalRepresentativeData::isActive).count()).isEqualTo(0);
            assertThat(bankGuaranteeRequestData.getLegalRepresentatives().stream().filter(LegalRepresentativeData::isSigner).count()).isEqualTo(0);
            assertThat(bankGuaranteeRequestData.getAlerDetails().getAlerErrorDetailData()).isNotNull();
            assertThat(bankGuaranteeRequestData.getAlerDetails().getTransactionId()).isEqualTo(errorSignatories.transactionId());
            assertThat(bankGuaranteeRequestData.getAlerDetails().getTransactionStatus()).isEqualTo(errorSignatories.transactionStatus());
            assertThat(bankGuaranteeRequestData.getAlerDetails().getTransactionStatus()).isEqualTo(SUCCESS_TRANSACTION_STATUS);
        }
    }

    @Test
    void checkUpdateAssociatedSignatoriesSoleProprietorPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getInstructingParty().getOrganisation().setLegalForm(LEGAL_FORM_SOLE_PROPRIETOR);
        AccessToken accessToken = MockHelper.getAccessToken(REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        IndividualOnePamResponse individualOnePamResponse = MockHelper.getIndividualEmailDigitalAddressOnePamResponse();
        given(involvePartyDetailsService.getInvolvePartyIndividualResp(anyString())).willReturn(CompletableFuture.completedFuture(individualOnePamResponse));
        given(involvePartyGranteeService.getLegalRepresentativeIds(anyString(), anyString())).willReturn(CompletableFuture.completedFuture(new ArrayList<>(List.of(REQUESTER_ID))));
        try (MockedStatic<CommonUtils> mockStatic = Mockito.mockStatic(CommonUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mockStatic.when(() -> CommonUtils.retrieveOnePamIdentifier(any(), any())).thenReturn(REQUESTER_ID);
            Void rvoid = alerSignatoriesProcessingService.updateAssociatedSignatories(accessToken, bankGuaranteeRequestData).join();
            LegalRepresentativeData signerLr = getSignerUUID(bankGuaranteeRequestData.getLegalRepresentatives());
            assertThat(bankGuaranteeRequestData.getAlerDetails()).isNotNull();
            assertThat(signerLr.isSigner()).isTrue();
            assertThat(signerLr.isFirstSigner()).isTrue();
            assertThat(signerLr.getUuid()).contains(bankGuaranteeRequestData.getAlerDetails().getSigners());
        }
    }


    private LegalRepresentativeData getSignerUUID(List<LegalRepresentativeData> legalRepresentativeDataList) {

        return legalRepresentativeDataList.stream()
                .filter(LegalRepresentativeData::isSigner)
                .findFirst()
                .orElse(null);
    }
}