package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.*;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeDocumentMapperTest {


    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";

    @Mock
    private CurrencyDetailService currencyService;

    @Mock
    private ConnectDotUtils connectDotUtils;

    @Mock
    private ConnectDotProperties connectDotProperties;

    @InjectMocks
    private BankGuaranteeDocumentMapper mapper;


    @BeforeEach
    void setup() {

        ApplicantDocumentPayload applicantDocumentPayload = MockHelper.getApplicantDocumentPayload();
        BeneficiaryDocumentPayload beneficiaryDocumentPayload = MockHelper.getBeneficiaryDocumentPayload();
        lenient().when(currencyService.getCurrencyCodeByValue(any())).thenReturn("EUR");
        lenient().when(connectDotUtils.prepareApplicant(any())).thenReturn(applicantDocumentPayload);
        lenient().when(connectDotUtils.prepareBeneficiary(any())).thenReturn(beneficiaryDocumentPayload);
        lenient().when(connectDotProperties.getTypeAmount()).thenReturn(Map.of("en", List.of("INITIAL", "ADDITIONAL"),
                "nl", List.of("OORSPRONKELIJK", "BIJKOMEND"),
                "fr", List.of("INITIAL", "SUPPLEMENTAIRE")));

    }

    @ParameterizedTest
    @MethodSource("bgCodes")
    void mapToBankGuaranteePayloadPositive(BankGuaranteeCode bankGuaranteeCode) {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        Locale locale = new Locale(bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode());
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData, DocumentType.BG_DRAFT);
        if (CommonUtils.isGuaranteeGenerationAllow(bankGuaranteeCode)) {
            BaseDocumentPayload baseDocumentPayload = mapper.mapToBankGuaranteePayload(connectDotInput);
            String referenceNumber = (bankGuaranteeCode == GOODS_TRANSPORT) ? "BankGuaranteeId" : bankGuaranteeRequestData.getReferenceNumber();
            assertThat(baseDocumentPayload).isNotNull();
            assertThat(baseDocumentPayload.getReferenceNumber()).isEqualTo(referenceNumber);
            assertThat(baseDocumentPayload.getModelType()).isEqualTo(bankGuaranteeCode == STATE_LOTTERY ? "LOTTO" : guaranteeDetails.getBgCode().name());
            assertThat(baseDocumentPayload.getBgAmount()).isEqualTo(guaranteeDetails.getBgAmount().doubleValue());
            assertThat(baseDocumentPayload.getBgLanguage()).isEqualTo(ConnectDotUtils.getDisplayLanguage(locale, locale));
            assertThat(baseDocumentPayload.getBgCurrency()).isEqualTo("EUR");
            assertThat(baseDocumentPayload.getPrintStatus()).isEqualTo(DocumentType.BG_DRAFT.getName());
            checkAssertion(bankGuaranteeCode, baseDocumentPayload, guaranteeDetails.getBankGuarantee());
        } else {
            BgosException bgosException = assertThrows(BgosException.class, () -> mapper.mapToBankGuaranteePayload(connectDotInput));
            assertNotNull(bgosException);
            assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
        }
    }

    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }

    private void checkAssertion(BankGuaranteeCode bankGuaranteeCode, BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        switch (bankGuaranteeCode) {
            case RENTAL -> assertRental(baseDocumentPayload, guaranteeType);
            case BID_BOND -> assertBidBomd(baseDocumentPayload, guaranteeType);
            case ADVANCE_PAYMENT -> assertAdvancePayment(baseDocumentPayload, guaranteeType);
            case PAYMENT_GUARANTEE -> assertPayment(baseDocumentPayload, guaranteeType);
            case PUBLIC_CONTRACT -> assertPublicContract(baseDocumentPayload, guaranteeType);
            case PERFORMANCE_BOND -> assertPerformanceBond(baseDocumentPayload, guaranteeType);
            case STATE_LOTTERY -> assertStateLottery(baseDocumentPayload, guaranteeType);
            case MONEY_RETENTION_BOND -> assertMoneyRetention(baseDocumentPayload, guaranteeType);
            case OVAM -> assertOvam(baseDocumentPayload, guaranteeType);
            case REAL_ESTATE -> assertRealEstate(baseDocumentPayload, guaranteeType);
            case CUSTOM_1 -> assertCustom1(baseDocumentPayload, guaranteeType);
            case CUSTOM_2 -> assertCustom2(baseDocumentPayload, guaranteeType);
            case CUSTOM_4 -> assertCustom4(baseDocumentPayload, guaranteeType);
            case CUSTOM_5 -> assertCustom5(baseDocumentPayload, guaranteeType);
            case DCK_CDC -> assertDck(baseDocumentPayload, guaranteeType);
            case WOODS_PROM_A -> assertStandardPromise(baseDocumentPayload, guaranteeType);
            case WOODS_PROM_B -> assertBlankPromise(baseDocumentPayload, guaranteeType);
            case WOODS_BGVLA_PUBLIC -> assertWoodsVLAPublic(baseDocumentPayload, guaranteeType);
            case GOODS_TRANSPORT -> assertGoodsTransport(baseDocumentPayload, guaranteeType);
            case PASSENGER_TRANSPORT -> assertPassengerTransport(baseDocumentPayload, guaranteeType);
            case OPERATORS_TRANSPORT -> assertTransportOperator(baseDocumentPayload, guaranteeType);

        }
    }

    private void assertCustom5(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
    }

    private void assertCustom4(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
    }

    private void assertCustom2(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {


    }

    private void assertCustom1(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        CustomOneDocumentPayload result = (CustomOneDocumentPayload) baseDocumentPayload;
        CustomTypeOne baseType = (CustomTypeOne) guaranteeType;
        assertThat(result.getAmountPercentage()).isEqualTo(String.valueOf(baseType.getAmountPercentage()));
    }

    private void assertRealEstate(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {

    }

    private void assertOvam(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        OvamDocumentPayload result = (OvamDocumentPayload) baseDocumentPayload;
        Ovam ovam = (Ovam) guaranteeType;
        assertThat(result.getBgExpiry()).isEqualTo(ovam.getBankGuaranteeEnd());

    }

    private void assertMoneyRetention(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        MoneyRetentionBondDocumentPayload result = (MoneyRetentionBondDocumentPayload) baseDocumentPayload;
        MoneyRetentionBond moneyRetentionBond = (MoneyRetentionBond) guaranteeType;
        assertThat(result.getContractReferenceNumber()).isEqualTo(moneyRetentionBond.getReferenceNumber());
        assertThat(result.getContractDescription()).isEqualTo(moneyRetentionBond.getContractDescription());
        assertThat(result.getBankAccountCredit()).isEqualTo(CommonUtils.formatIbanNumber(moneyRetentionBond.getAdvancePaymentIBAN()));
    }

    private void assertStateLottery(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        StateLotteryDocumentPayload result = (StateLotteryDocumentPayload) baseDocumentPayload;
        StateLottery baseType = (StateLottery) guaranteeType;
        assertThat(result.getContractSignDate()).isEqualTo(baseType.getDateOfAgreeInPrinc());
    }

    private void assertPerformanceBond(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        PerformanceBondDocumentPayload result = (PerformanceBondDocumentPayload) baseDocumentPayload;
        PerformanceBond baseType = (PerformanceBond) guaranteeType;
        Double bgAmountFiftyPercentUp = ConnectDotUtils.getBgAmountFiftyPercentUp(BigDecimal.valueOf(result.getBgAmount()));
        Double bgAmountFiftyPercentDown = ConnectDotUtils.getBgAmountFiftyPercentDown(BigDecimal.valueOf(result.getBgAmount()));
        assertThat(result.getContractReferenceNumber()).isEqualTo(baseType.getReferenceNumber());
        assertThat(result.getContractDescription()).isEqualTo(baseType.getContractDescription());
        assertThat(result.getBgFiftyPercentAmountRoundedDown()).isEqualTo(bgAmountFiftyPercentDown);
        assertThat(result.getBgFiftyPercentAmountRoundedUp()).isEqualTo(bgAmountFiftyPercentUp);
        assertThat(result.getAmountInLetterRoundedDown()).isEqualTo(bgAmountFiftyPercentDown);
        assertThat(result.getAmountInLetterRoundedUp()).isEqualTo(bgAmountFiftyPercentUp);
    }

    private void assertPublicContract(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        PublicContractDocumentPayload result = (PublicContractDocumentPayload) baseDocumentPayload;
        PublicContract baseType = (PublicContract) guaranteeType;
        assertThat(result.getContractDescription()).isEqualTo(baseType.getTitle());
        assertThat(result.getGrantDate()).isEqualTo(baseType.getGrantDate());
        assertThat(result.getContractReferenceNumber()).isEqualTo(baseType.getReferenceNumber());
        assertThat(result.getTotalAmount()).isEqualTo(baseType.getTotalAmount().doubleValue());
    }

    private void assertPayment(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        PaymentGuaranteeDocumentPayload result = (PaymentGuaranteeDocumentPayload) baseDocumentPayload;
        Payment baseType = (Payment) guaranteeType;
        assertThat(result.getContractReferenceNumber()).isEqualTo(baseType.getReferenceNumber());
        assertThat(result.getContractDescription()).isEqualTo(baseType.getContractDescription());
        assertThat(result.getBgExpiry()).isEqualTo(baseType.getBankGuaranteeEnd());
    }

    private void assertAdvancePayment(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        AdvancePaymentDocumentPayload result = (AdvancePaymentDocumentPayload) baseDocumentPayload;
        AdvancePayment baseType = (AdvancePayment) guaranteeType;
        assertThat(result.getContractReferenceNumber()).isEqualTo(baseType.getReferenceNumber());
        assertThat(result.getContractDescription()).isEqualTo(baseType.getContractDescription());
    }

    private void assertBidBomd(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {

        BidBondDocumentPayload bidBondDocumentPayload = (BidBondDocumentPayload) baseDocumentPayload;
        BidBond bidBond = (BidBond) guaranteeType;
        assertThat(bidBondDocumentPayload.getContractReferenceNumber()).isEqualTo(bidBond.getReferenceNumber());
        assertThat(bidBondDocumentPayload.getContractDescription()).isEqualTo(bidBond.getContractDescription());
    }

    private void assertRental(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        RentalDocumentPayload result = (RentalDocumentPayload) baseDocumentPayload;
        Rental baseType = (Rental) guaranteeType;
        Integer rentalGracePeriod = CommonUtils.getRentalGracePeriod(baseType.getExpiryDate());
        assertThat(result.getCity()).isEqualTo(baseType.getCity());
        assertThat(result.getStreet()).isEqualTo(baseType.getStreet());
        assertThat(result.getPostalCode()).isEqualTo(baseType.getPostalCode());
        assertThat(result.getDateOfSignature()).isEqualTo(baseType.getDateOfSignature());
        assertThat(result.getContractEndDate()).isEqualTo(baseType.getContractEndDate());
        assertThat(result.getRentalContractEndDate()).isEqualTo(baseType.getContractEndDate().plusDays(rentalGracePeriod));
        assertThat(result.getGracePeriod()).isEqualTo(String.valueOf(rentalGracePeriod));
    }

    private void assertDck(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
    }

    private void assertStandardPromise(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        StandardPromiseDocumentPayload result = (StandardPromiseDocumentPayload) baseDocumentPayload;
        WoodsWalloniaStandardPromise woodsWalloniaStandardPromise = (WoodsWalloniaStandardPromise) guaranteeType;
        assertThat(result.getContractCity()).isEqualTo(woodsWalloniaStandardPromise.getSalePlace());

    }

    private void assertBlankPromise(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        WoodsBlankPromisePayload result = (WoodsBlankPromisePayload) baseDocumentPayload;
        WoodsBlankPromise woodsBlankPromise = (WoodsBlankPromise) guaranteeType;
        assertThat(result.getContractEndDate()).isEqualTo(woodsBlankPromise.getPromiseEndDate());
    }

    private void assertWoodsVLAPublic(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        WoodsVLADocumentPayload result = (WoodsVLADocumentPayload) baseDocumentPayload;
        WoodsVLAPublic woodsVLAPublic = (WoodsVLAPublic) guaranteeType;
        assertThat(result.getDeadlineOne()).isEqualTo(woodsVLAPublic.getFirstDeadline());
    }

    private void assertGoodsTransport(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        GoodsTransportDocumentPayload result = (GoodsTransportDocumentPayload) baseDocumentPayload;
        GoodsTransport passengerTransport = (GoodsTransport) guaranteeType;
        assertThat(result.getLicenseNumber()).isEqualTo(passengerTransport.getLicenseNumber());
    }

    private void assertPassengerTransport(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        PassengerTransportDocumentPayload result = (PassengerTransportDocumentPayload) baseDocumentPayload;
        PassengerTransport passengerTransport = (PassengerTransport) guaranteeType;
        assertThat(result.getLicenseNumber()).isEqualTo(passengerTransport.getLicenseNumber());
    }

    private void assertTransportOperator(BaseDocumentPayload baseDocumentPayload, BaseGuaranteeType guaranteeType) {
        OperatorTransportDocumentPayload result = (OperatorTransportDocumentPayload) baseDocumentPayload;
        OperatorsTransport goodsTransport = (OperatorsTransport) guaranteeType;
        assertThat(result.getProfession()).isEqualTo(goodsTransport.getTransportOperatorActivityType().getEnTranslation());
    }
}