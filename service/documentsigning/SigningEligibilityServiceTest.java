package com.ing.bankguarantees.service.documentsigning;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.guaranteetype.BidBond;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.emeafx.model.request.FxRatesConversionInput;
import com.ing.bankguarantees.remote.rest.emeafx.model.response.FxRateConversionResponse;
import com.ing.bankguarantees.service.bankguarantee.BankGuaranteeService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SigningEligibilityServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String ALER_SIGNATORY_RESPONSE = "BGA/SSA/aler_signatories_data.json";
    private static final String FAILED_TRANSACTION_STATUS = "FAILED";
    private static final String REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4eb";
    private static final String SECOND_SIGNATORY_ID = "d8f80f8d-1b90-4ad7-b8c5-3b4d22e8c864";
    private static final String PASSIVE_SIGNATORY_ID = "d8f80f8d-1b90-4ad7-b8c5-3b4d22e8c889";
    private static final String INVALID_REQUESTER_ID = "e20b9980-cbc3-4ae6-beae-77fe39eee4ak34";
    private static final String UUID_ORG = "3f97bf90-9dd9-4acb-b99a-d4c49be71ece";
    private static final String SESSION_ID = UUID.randomUUID().toString();


    @Mock
    private CurrencyDetailService currencyDetailService;

    @Mock
    private BankGuaranteeService bankGuaranteeService;

    @Mock
    private ClientGateway<FxRatesConversionInput, BigDecimal, FxRateConversionResponse> fxRateConversionClientGateway;

    @InjectMocks
    private SigningEligibilityService signingEligibilityService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(signingEligibilityService, "defaultCurrency", "003");
        ReflectionTestUtils.setField(signingEligibilityService, "defaultCurrencyValue", "EUR");
    }


    @Test
    void checkAllowSigningPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isTrue();
        assertThat(stpResultData.getJustification()).isEqualTo("customer is allowed to sign the documents.");
    }

    @Test
    void checkAllowSigningAmountCapExceededAndMoreThanTwoLegalRepresentatives() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getLegalRepresentatives().forEach(lr -> lr.setActive(true));
        bankGuaranteeRequestData.setAmountCapExceed(true);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("signing not allow because Amount of guarantee is more than 100000 EUR and LR count >= 3.");
    }

    @Test
    void checkAllowSigningWBCustomer() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setWbCustomer(true);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("Requester is whole sale banking customer.");
    }

    @Test
    void checkAllowSigningBidBondSelectedAsOther() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.BID_BOND);
        BidBond bankGuarantee = (BidBond) guaranteeDetails.getBankGuarantee();
        bankGuarantee.setBankGuaranteeEndType(BankGuaranteeEndType.OTHER);
        guaranteeDetails.setBgCode(BankGuaranteeCode.BID_BOND);
        guaranteeDetails.setBgAmount(bankGuaranteeRequestData.getGuaranteeDetails().getBgAmount());
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("Requester selected bid bond guarantee end type as other.");
    }

    @Test
    void checkAllowSigningForInvalidIDs() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        Optional<LegalRepresentativeData> signer = bankGuaranteeRequestData.getLegalRepresentatives().stream().filter(LegalRepresentativeData::isSigner).findFirst();
        signer.get().setInternalIdentifiers(List.of());
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("Signer important kyc ids(CSI ID  & ING ID) are not present.");
    }

    @Test
    void checkAllowSigningForAlerTransactionFailed() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AlerDetailsData alerDetails = bankGuaranteeRequestData.getAlerDetails();
        alerDetails.setTransactionStatus("FAILED");
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.allowSigningStpCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo(alerDetails.getAlerErrorDetailData().getCode() + " : " + alerDetails.getAlerErrorDetailData().getMessage());
    }

    @Test
    void signingEligibilityCheckPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.signingEligibilityCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isTrue();
        assertThat(stpResultData.getJustification()).isEqualTo("All signers  have complete authority to sign the documents");
    }


    @Test
    void signingPowerCheckSigningNotAllow() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setLegalRepresentatives(Collections.emptyList());
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.signingEligibilityCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("Signing is not allowed for this request");
    }

    @Test
    void signingEligibilityCheckSingleLR() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        LegalRepresentativeData legalRepresentativeData = bankGuaranteeRequestData.getLegalRepresentatives().get(1);
        bankGuaranteeRequestData.getLegalRepresentatives().remove(legalRepresentativeData);
        bankGuaranteeRequestData.getLegalRepresentatives().get(0).setSigningPower(2);
        StpResultDataSet.STPResultData stpResultData = signingEligibilityService.signingEligibilityCheck(bankGuaranteeRequestData);
        assertThat(stpResultData).isNotNull();
        assertThat(stpResultData.isStpPossible()).isFalse();
        assertThat(stpResultData.getJustification()).isEqualTo("signers don't  have complete authority to sign the documents");
    }

    @Test
    void checkAlerEligibilityPositive() {
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PERFORMANCE_BOND);
        Boolean result = signingEligibilityService.checkAlerEligibility(alerSignatories, false, guaranteeDetails).join();
        assertThat(result).isFalse();
    }

    @Test
    void checkAlerEligibilityAmountCapExceeded() {
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PERFORMANCE_BOND);
        guaranteeDetails.setBgAmount(BigDecimal.valueOf(10000010));
        Boolean result = signingEligibilityService.checkAlerEligibility(alerSignatories, false, guaranteeDetails).join();
        assertThat(result).isTrue();
    }


    @Test
    void checkAlerEligibilityWholeSaleBankingCustomer() {
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PERFORMANCE_BOND);
        Boolean result = signingEligibilityService.checkAlerEligibility(alerSignatories, true, guaranteeDetails).join();
        assertThat(result).isTrue();
    }

    @Test
    void checkAlerEligibilityBidBond() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.BID_BOND);
        BidBond bankGuarantee = (BidBond) guaranteeDetails.getBankGuarantee();
        bankGuarantee.setBankGuaranteeEndType(BankGuaranteeEndType.OTHER);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        Boolean result = signingEligibilityService.checkAlerEligibility(alerSignatories, true, guaranteeDetails).join();
        assertThat(result).isTrue();
    }

    @Test
    void checkAlerEligibilityForSingleLR() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.BID_BOND);
        BidBond bankGuarantee = (BidBond) guaranteeDetails.getBankGuarantee();
        bankGuarantee.setBankGuaranteeEndType(BankGuaranteeEndType.OTHER);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        AlerSignatories newSignatories = MockHelper.getAlerSignatories(alerSignatories.transactionId(), alerSignatories.transactionStatus(),
                List.of(alerSignatories.signatories().get(0)), null);
        Boolean result = signingEligibilityService.checkAlerEligibility(newSignatories, true, guaranteeDetails).join();
        assertThat(result).isTrue();
    }


    @Test
    void checkAmountCapRulePositive() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        guaranteeDetails.setBgCurrency("031");
        given(fxRateConversionClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(BigDecimal.ONE));
        Boolean result = signingEligibilityService.checkAmountCapRule(guaranteeDetails).join();
        assertThat(result).isFalse();
    }

    @Test
    void checkAmountCapRuleError() {
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.PUBLIC_CONTRACT);
        guaranteeDetails.setBgCurrency("031");
        given(fxRateConversionClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Boolean> future = signingEligibilityService.checkAmountCapRule(guaranteeDetails);
        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    void getSignersPositive() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEqualTo(bankGuaranteeRequestData.getAlerDetails().getSigners());

    }

    @Test
    void getSignersInvokeAlerFalse() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getAlerDetails().setInvokeAler(false);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEqualTo(alerSignatories.signatories().stream().map(AlerSignatories.Signatory::signatoryUUID).toList());

    }

    @Test
    void getSignersBidBondGuarantee() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getAlerDetails().setSigners(List.of());
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(BankGuaranteeCode.BID_BOND);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEmpty();

    }

    @Test
    void getSignersAlerNotPossible() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getAlerDetails().setSigners(List.of());
        AlerSignatories alerSignatories = AlerSignatories.builder().transactionStatus(FAILED_TRANSACTION_STATUS).signatories(List.of()).build();
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEmpty();

    }

    @Test
    void getSignersForWbCustomer() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getAlerDetails().setSigners(List.of());
        bankGuaranteeRequestData.setWbCustomer(true);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEmpty();

    }

    @Test
    void getSignersFor() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getAlerDetails().setSigners(List.of());
        bankGuaranteeRequestData.setWbCustomer(true);
        AlerSignatories alerSignatories = MockHelper.createAlerSignatories(ALER_SIGNATORY_RESPONSE);
        List<String> signers = signingEligibilityService.getSigners(alerSignatories, bankGuaranteeRequestData);
        assertThat(signers).isEmpty();

    }

}
