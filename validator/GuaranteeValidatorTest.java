package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.BankGuaranteeDataSet.BankGuaranteeData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.CoverType;
import com.ing.bankguarantees.models.enums.RegionCode;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.models.request.GuaranteeDetailsPayload;
import com.ing.bankguarantees.service.bankguarantee.BankGuaranteeService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.validation.validator.GuaranteeValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static com.ing.bankguarantees.util.MockHelper.getGuaranteeDetailsPayload;
import static com.ing.bankguarantees.utils.ConstantUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuaranteeValidatorTest {

    private final static String INVALID_SCOPE_MESSAGE = "Invalid Bank guarantee code BID_BOND ";

    @Mock
    private BankGuaranteeService bankGuaranteeService;

    private GuaranteeValidator guaranteeValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setup() {

    }

    @ParameterizedTest
    @MethodSource("bgCodes")
    void validateGuaranteePositive(BankGuaranteeCode bankGuaranteeCode) {
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, bankGuaranteeCode.name());
        guaranteeValidator = new GuaranteeValidator(bankGuaranteeService);
        lenient().when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        lenient().when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        lenient().when(bankGuaranteeService.getMatchedBankGuaranteeData(any())).thenReturn(bankGuaranteeData);
        GuaranteeDetailsPayload<BaseGuaranteeType> guaranteeDetailsPayload = getGuaranteeDetailsPayload(bankGuaranteeCode);
        boolean valid = guaranteeValidator.isValidGuarantee(guaranteeDetailsPayload, constraintValidatorContext);
        assertThat(valid).isTrue();
    }

    @Test
    void invalidScopeGuarantee() {
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(false, BankGuaranteeCode.BID_BOND.name());
        guaranteeValidator = new GuaranteeValidator(bankGuaranteeService);
        when(bankGuaranteeService.getMatchedBankGuaranteeData(any())).thenReturn(bankGuaranteeData);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        GuaranteeDetailsPayload<BaseGuaranteeType> guaranteeDetailsPayload = new GuaranteeDetailsPayload<>();
        guaranteeDetailsPayload.setBgCode(BankGuaranteeCode.BID_BOND);
        guaranteeDetailsPayload.setBankGuarantee(MockHelper.getBidBond());
        boolean result = guaranteeValidator.isValidGuarantee(guaranteeDetailsPayload, constraintValidatorContext);
        ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(constraintValidatorContext, times(1)).buildConstraintViolationWithTemplate(errorMessageCaptor.capture());
        assertEquals(INVALID_SCOPE_MESSAGE, errorMessageCaptor.getValue());
        assertThat(result).isFalse();
    }

    @ParameterizedTest(name = "Invalid guarantee details :  {0}")
    @MethodSource(value = "invalidGuaranteeDetails")
    void invalidGuaranteeDetails(String description, String errorMessage, BankGuaranteeCode bankGuaranteeCode, BaseGuaranteeType guaranteeType, int wantedNumberOfInvocations) {
        BankGuaranteeData bankGuaranteeData = MockHelper.getBankGuaranteeData(true, bankGuaranteeCode.name());
        lenient().when(bankGuaranteeService.getMatchedBankGuaranteeData(any())).thenReturn(bankGuaranteeData);
        guaranteeValidator = new GuaranteeValidator(bankGuaranteeService);
        GuaranteeDetailsPayload<BaseGuaranteeType> guaranteeDetailsPayload = new GuaranteeDetailsPayload<>();
        guaranteeDetailsPayload.setBgCode(bankGuaranteeCode);
        guaranteeDetailsPayload.setBankGuarantee(guaranteeType);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        boolean result = guaranteeValidator.isValidGuarantee(guaranteeDetailsPayload, constraintValidatorContext);
        ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(constraintValidatorContext, times(wantedNumberOfInvocations)).buildConstraintViolationWithTemplate(errorMessageCaptor.capture());
        if (wantedNumberOfInvocations > 1) {
            assertEquals(errorMessage, errorMessageCaptor.getAllValues().get(0));
        } else {
            assertEquals(errorMessage, errorMessageCaptor.getValue());
        }
        assertThat(result).isFalse();
    }


    private static Stream<Arguments> invalidGuaranteeDetails() {
        return Stream.of(
                Arguments.of("Public contract invalid expiryDate ", "expiryDate must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getExpryDateEmptyPublicContract(), 1),
                Arguments.of("Public contract invalid title", "title must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getTitleNullPublicContract(), 1),
                Arguments.of("Public contract invalid grantDate", "grantDate must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getGranteDateNullPublicContract(), 1),
                Arguments.of("Public contract invalid referenceNumber", "referenceNumber must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getReferenceNumberNullPublicContract(), 1),
                Arguments.of("Public contract invalid totalAmount", "totalAmount must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getTotalAmountNullPublicContract(), 1),
                Arguments.of("Public contract invalid tenderSpecification", "tenderSpecification must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT, getTenderSpecificationNullPublicContract(), 1),

                Arguments.of("Bid Bond invalid contractDescription", "contractDescription must not be empty or null", BankGuaranteeCode.BID_BOND, getContractDescriptionNullBidBond(), 1),
                Arguments.of("Bid Bond invalid referenceNumber", "referenceNumber must not be empty or null", BankGuaranteeCode.BID_BOND, getReferenceNumberNullBidBond(), 1),
                Arguments.of("Bid Bond invalid bankGuaranteeEnd", "bankGuaranteeEnd must not be empty or null", BankGuaranteeCode.BID_BOND, getRBankGuaranteeEndNullBidBond(), 2),
                Arguments.of("Bid Bond invalid maturityDate", "maturityDate must not be empty or null", BankGuaranteeCode.BID_BOND, getMaturityDateNullBidBond(), 1),

                Arguments.of("Performance Bond invalid contractDescription", "contractDescription must not be empty or null", BankGuaranteeCode.PERFORMANCE_BOND, getContractDescriptionNullPerformanceBond(), 1),
                Arguments.of("Performance Bond invalid referenceNumber", "referenceNumber must not be empty or null", BankGuaranteeCode.PERFORMANCE_BOND, getReferenceNumberNullPerformanceBond(), 1),
                Arguments.of("Performance Bond invalid bankGuaranteeEnd", "bankGuaranteeEnd must not be empty or null", BankGuaranteeCode.PERFORMANCE_BOND, getBankGuaranteeEndNullPerformanceBond(), 2),
                Arguments.of("Performance Bond invalid maturityDate", "maturityDate must not be empty or null", BankGuaranteeCode.PERFORMANCE_BOND, getMaturityDateNullPerformanceBond(), 1),
                Arguments.of("Performance Bond invalid finalMaturityDate", "finalMaturityDate must not be empty or null", BankGuaranteeCode.PERFORMANCE_BOND, getFinalMaturityDateNullPerformanceBond(), 1),

                Arguments.of("Payment invalid contractDescription", "contractDescription must not be empty or null", BankGuaranteeCode.PAYMENT_GUARANTEE, getContractDescriptionNullPayment(), 1),
                Arguments.of("Payment invalid referenceNumber", "referenceNumber must not be empty or null", BankGuaranteeCode.PAYMENT_GUARANTEE, getReferenceNumberNullPayment(), 1),
                Arguments.of("Payment invalid bankGuaranteeEnd", "bankGuaranteeEnd must not be empty or null", BankGuaranteeCode.PAYMENT_GUARANTEE, getBankGuaranteeEndNullPayment(), 2),
                Arguments.of("Payment invalid maturityDate", "maturityDate must not be empty or null", BankGuaranteeCode.PAYMENT_GUARANTEE, getMaturityDateNullPayment(), 1),

                Arguments.of("Standard Promise invalid endDate", "promiseEndDate must not be empty or null", BankGuaranteeCode.WOODS_PROM_A, getPromiseEndDateNullStandardPromise(), 1),
                Arguments.of("Standard Promise invalid salePlace", "salePlace must not be empty or null", BankGuaranteeCode.WOODS_PROM_A, getSalePlaceNullStandardPromise(), 1),
                Arguments.of("Standard Promise invalid saleDate", "saleDate must not be empty or null", BankGuaranteeCode.WOODS_PROM_A, getSaleDateNullStandardPromise(), 1),

                Arguments.of("Standard Promise invalid saleDate plus months For Flanders", "For Flanders region, promiseEndDate should be system date plus eight months", BankGuaranteeCode.WOODS_PROM_VLA, getPromiseEndDateFlandersNotCorrectMonthsStandardPromise(), 1),
                Arguments.of("Standard Promise invalid saleDate plus months For Wallonia", "region must be flanders", BankGuaranteeCode.WOODS_PROM_VLA, getPromiseEndDateWalloniaNotCorrectMonthsStandardPromise(), 1),

                Arguments.of("Blank Promise invalid endDate", "promiseEndDate must not be empty or null", BankGuaranteeCode.WOODS_PROM_B, getPromiseEndDateNullBlankPromise(), 1),
                Arguments.of("Blank Promise invalid promiseEndDate", "promiseEndDate should be system date plus eight months", BankGuaranteeCode.WOODS_PROM_B, getPromiseEndDateFlandersNotCorrectMonthsBlankPromise(), 1),

                Arguments.of("Woods Wallonia Public invalid bankGuaranteeEndType", "bankGuaranteeEndType must be unspecified", BankGuaranteeCode.WOODS_BGWAL_PUBLIC, getBankGuaranteeEndDateSpecifiedWoodsWalloniaPublic(), 1),
                Arguments.of("Woods Wallonia Public empty promiseIds", "promise Id/Ids must not be empty or null", BankGuaranteeCode.WOODS_BGWAL_PUBLIC, getPromiseIdsEmptyWoodsWalloniaPublic(), 1),
                Arguments.of("Woods Wallonia Public invalid promiseIds", "promise Id/Ids must have 16 character length", BankGuaranteeCode.WOODS_BGWAL_PUBLIC, getPromiseIdsInvalidWoodsWalloniaPublic(), 1),
                Arguments.of("Woods Wallonia Public invalid regionCode", "region must be Wallonia", BankGuaranteeCode.WOODS_BGWAL_PUBLIC, getRegionInvalidWoodsWalloniaPublic(), 1),

                Arguments.of("Woods Flanders Public invalid regionCode", "region must be Flanders", BankGuaranteeCode.WOODS_BGVLA_PUBLIC, getRegionInvalidWoodsFlandersPublic(), 1),

                Arguments.of("Woods Wallonia private sale invalid maturityDate", "maturityDate must be last deadline plus 365 days", BankGuaranteeCode.WOODS_BG_PRIVATE, getMaturityDateInvalidWoodsWalloniaPrivateSale(), 1),
                Arguments.of("Woods France private sale invalid maturityDate", "maturityDate must be last deadline plus 365 days", BankGuaranteeCode.WOODS_BG_PRIVATE, getMaturityDateInvalidWoodsFrancePrivateSale(), 1),

                Arguments.of("Abstract Promise null promiseEndDate", "promiseEndDate must not be empty or null", BankGuaranteeCode.ABSTRACT_PROM, getPromiseEndDateNullPromiseAbstract(), 1),
                Arguments.of("Abstract Promise invalid contractDescription", "contractDescription must not be empty or null", BankGuaranteeCode.ABSTRACT_PROM, getContractDescriptionNullPromiseAbstract(), 1),
                Arguments.of("Abstract Promise invalid referenceNumber", "referenceNumber must not be empty or null", BankGuaranteeCode.ABSTRACT_PROM, getReferenceNumberNullPromiseAbstract(), 1),
                Arguments.of("Abstract Promise invalid promiseEndDate", "promiseEndDate should be system date plus six months", BankGuaranteeCode.ABSTRACT_PROM, getPromiseEndDateInvalidPromiseAbstract(), 1),

                Arguments.of("Abstract Public Contract Promise null promiseEndDate", "promiseEndDate must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT_PROM, getPromiseEndDateNullPromisePublicContract(), 1),
                Arguments.of("Abstract Public Contract Promise invalid title", "title of the public contract must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT_PROM, getTitleNullPromisePublicContract(), 1),
                Arguments.of("Abstract Public Contract Promise invalid contractDescription", "contractDescription must not be empty or null", BankGuaranteeCode.PUBLIC_CONTRACT_PROM, getReferenceNumberNullPromisePublicContract(), 1),
                Arguments.of("Abstract Public Contract Promise invalid promiseEndDate", "promiseEndDate should be system date plus six months", BankGuaranteeCode.PUBLIC_CONTRACT_PROM, getPromiseEndDateInvalidPromisePublicContract(), 1)

        );
    }

    private static PublicContract getExpryDateEmptyPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setExpiryDate(null);
        return publicContractDetails;
    }

    private static PublicContract getTitleNullPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setTitle(null);
        return publicContractDetails;
    }

    private static PublicContract getGranteDateNullPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setGrantDate(null);
        return publicContractDetails;
    }

    private static PublicContract getReferenceNumberNullPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setReferenceNumber(null);
        return publicContractDetails;
    }

    private static PublicContract getTenderSpecificationNullPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setTenderSpecification(null);
        return publicContractDetails;
    }

    private static PublicContract getTotalAmountNullPublicContract() {
        PublicContract publicContractDetails = MockHelper.getPublicContractDetails();
        publicContractDetails.setTotalAmount(null);
        return publicContractDetails;
    }

    private static BidBond getReferenceNumberNullBidBond() {
        BidBond bidBond = MockHelper.getBidBond();
        bidBond.setReferenceNumber(null);
        return bidBond;
    }

    private static BidBond getContractDescriptionNullBidBond() {
        BidBond bidBond = MockHelper.getBidBond();
        bidBond.setContractDescription(null);
        return bidBond;
    }

    private static BidBond getRBankGuaranteeEndNullBidBond() {
        BidBond bidBond = MockHelper.getBidBond();
        bidBond.setBankGuaranteeEnd(null);
        bidBond.setBankGuaranteeEndType(null);
        return bidBond;
    }

    private static BidBond getMaturityDateNullBidBond() {
        BidBond bidBond = MockHelper.getBidBond();
        bidBond.setBankGuaranteeEnd(SPECIFIED_TEXT);
        bidBond.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        bidBond.setMaturityDate(null);
        return bidBond;
    }


    private static BaseGuaranteeType getContractDescriptionNullPerformanceBond() {
        PerformanceBond performanceBond = MockHelper.getPerformanceBondDetails();
        performanceBond.setContractDescription(null);
        return performanceBond;
    }


    private static BaseGuaranteeType getReferenceNumberNullPerformanceBond() {
        PerformanceBond performanceBond = MockHelper.getPerformanceBondDetails();
        performanceBond.setReferenceNumber(null);
        return performanceBond;
    }

    private static BaseGuaranteeType getBankGuaranteeEndNullPerformanceBond() {
        PerformanceBond performanceBond = MockHelper.getPerformanceBondDetails();
        performanceBond.setBankGuaranteeEnd(null);
        performanceBond.setBankGuaranteeEndType(null);
        return performanceBond;
    }

    private static BaseGuaranteeType getMaturityDateNullPerformanceBond() {
        PerformanceBond performanceBond = MockHelper.getPerformanceBondDetails();
        performanceBond.setBankGuaranteeEnd(SPECIFIED_TEXT);
        performanceBond.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        performanceBond.setMaturityDate(null);
        return performanceBond;
    }

    private static BaseGuaranteeType getFinalMaturityDateNullPerformanceBond() {
        PerformanceBond performanceBond = MockHelper.getPerformanceBondDetails();
        performanceBond.setBankGuaranteeEnd(PERFORMANCE_BOND_FINAL_MATURITY_DATE_TEXT);
        performanceBond.setBankGuaranteeEndType(BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE);
        performanceBond.setFinalMaturityDate(null);
        return performanceBond;
    }

    private static BaseGuaranteeType getContractDescriptionNullPayment() {
        Payment payment = MockHelper.getPaymentDetails();
        payment.setContractDescription(null);
        return payment;
    }

    private static BaseGuaranteeType getReferenceNumberNullPayment() {
        Payment payment = MockHelper.getPaymentDetails();
        payment.setReferenceNumber(null);
        return payment;
    }

    private static BaseGuaranteeType getBankGuaranteeEndNullPayment() {
        Payment payment = MockHelper.getPaymentDetails();
        payment.setBankGuaranteeEnd(null);
        payment.setBankGuaranteeEndType(null);
        return payment;
    }

    private static BaseGuaranteeType getMaturityDateNullPayment() {
        Payment payment = MockHelper.getPaymentDetails();
        payment.setBankGuaranteeEnd(SPECIFIED_TEXT);
        payment.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        payment.setMaturityDate(null);
        return payment;
    }

    private static BaseGuaranteeType getPromiseEndDateNullStandardPromise() {
        WoodsWalloniaStandardPromise promise = (WoodsWalloniaStandardPromise) MockHelper.getStandardPromiseDetails();
        promise.setPromiseEndDate(null);
        return promise;
    }

    private static BaseGuaranteeType getSalePlaceNullStandardPromise() {
        WoodsWalloniaStandardPromise promise = (WoodsWalloniaStandardPromise) MockHelper.getStandardPromiseDetails();
        promise.setSalePlace(null);
        return promise;
    }

    private static BaseGuaranteeType getSaleDateNullStandardPromise() {
        WoodsWalloniaStandardPromise promise = (WoodsWalloniaStandardPromise) MockHelper.getStandardPromiseDetails();
        promise.setSaleDate(null);
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateFlandersNotCorrectMonthsStandardPromise() {
        WoodsFlandersStandardPromise promise = (WoodsFlandersStandardPromise) MockHelper.getFlandersStandardPromiseDetails();
        promise.setBankGuaranteeEnd(SPECIFIED_TEXT);
        promise.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        promise.setPromiseEndDate(LocalDate.now().plusMonths(WOODS_PROMISE_SYSTEM_DATE_EXTRA_MONTHS + 3L));
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateWalloniaNotCorrectMonthsStandardPromise() {
        WoodsFlandersStandardPromise promise = (WoodsFlandersStandardPromise) MockHelper.getFlandersStandardPromiseDetails();
        promise.setRegion(RegionCode.WALLONIA);
        promise.setPromiseEndDate(LocalDate.now().plusMonths(WOODS_PROMISE_SALE_DATE_EXTRA_MONTHS + 4L));
        return promise;
    }

    private static Object getPromiseEndDateFlandersNotCorrectMonthsBlankPromise() {
        WoodsBlankPromise promise = MockHelper.getBlankPromiseDetails();
        promise.setPromiseEndDate(LocalDate.now().plusMonths(WOODS_PROMISE_SYSTEM_DATE_EXTRA_MONTHS + 3L));
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateNullBlankPromise() {
        WoodsBlankPromise promise = MockHelper.getBlankPromiseDetails();
        promise.setPromiseEndDate(null);
        return promise;
    }

    private static Object getBankGuaranteeEndNullWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setBankGuaranteeEndType(null);
        return woodsWALPublic;
    }

    private static Object getPromiseIdsEmptyWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setPromiseIds(null);
        return woodsWALPublic;
    }

    private static Object getPromiseIdsInvalidWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setPromiseIds("BGOW25071215144,BG1,BG2");
        return woodsWALPublic;
    }

    private static Object getRegionInvalidWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setRegion(RegionCode.FRANCE);
        return woodsWALPublic;
    }

    private static Object getRegionInvalidWoodsFlandersPublic() {
        WoodsVLAPublic woodsWALPublic = MockHelper.getWoodsVLAPublicDetails();
        woodsWALPublic.setRegion(RegionCode.FRANCE);
        return woodsWALPublic;
    }

    private static Object getFirstTransactionAmountNullWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setFirstTransactionAmt(null);
        return woodsWALPublic;
    }

    private static Object getSecondTransactionAmountNullWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setSecondTransactionAmt(null);
        return woodsWALPublic;
    }

    private static Object getFirstDeadlineNullWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setFirstDeadline(null);
        return woodsWALPublic;
    }

    private static Object getSecondDeadlineNullWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setSecondDeadline(null);
        return woodsWALPublic;
    }

    private static Object getBankGuaranteeEndDateSpecifiedWoodsWalloniaPublic() {
        WoodsWALPublic woodsWALPublic = MockHelper.getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
        woodsWALPublic.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        woodsWALPublic.setBankGuaranteeEnd(SPECIFIED_TEXT);
        return woodsWALPublic;
    }

    private static Object getMaturityDateInvalidWoodsWalloniaPrivateSale() {
        WoodsPrivate woodsPrivate = MockHelper.getWoodsWalloniaPrivateSaleDetails();
        woodsPrivate.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        woodsPrivate.setBankGuaranteeEnd(SPECIFIED_TEXT);
        woodsPrivate.setMaturityDate(LocalDate.now());
        return woodsPrivate;
    }

    private static Object getMaturityDateInvalidWoodsFrancePrivateSale() {
        WoodsPrivate woodsPrivate = MockHelper.getWoodsWalloniaPrivateSaleDetails();
        woodsPrivate.setRegion(RegionCode.FRANCE);
        woodsPrivate.setBankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED);
        woodsPrivate.setBankGuaranteeEnd(SPECIFIED_TEXT);
        woodsPrivate.setMaturityDate(LocalDate.now());
        woodsPrivate.setResidualAmount(BigDecimal.ONE);
        return woodsPrivate;
    }

    private static BaseGuaranteeType getPromiseEndDateNullPromiseAbstract() {
        PromiseAbstract promise = (PromiseAbstract) MockHelper.getAbstractPromiseDetails();
        promise.setPromiseEndDate(null);
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateInvalidPromiseAbstract() {
        PromiseAbstract promise = (PromiseAbstract) MockHelper.getAbstractPromiseDetails();
        promise.setPromiseEndDate(LocalDate.now().plusMonths(ABSTRACT_PROMISE_SYSTEM_DATE_EXTRA_MONTHS + 4L));
        return promise;
    }

    private static BaseGuaranteeType getContractDescriptionNullPromiseAbstract() {
        PromiseAbstract promise = (PromiseAbstract) MockHelper.getAbstractPromiseDetails();
        promise.setContractDescription(null);
        return promise;
    }

    private static BaseGuaranteeType getReferenceNumberNullPromiseAbstract() {
        PromiseAbstract promise = (PromiseAbstract) MockHelper.getAbstractPromiseDetails();
        promise.setReferenceNumber(null);
        return promise;
    }

    private static BaseGuaranteeType getTitleNullPromisePublicContract() {
        PromisePublicContract promise = (PromisePublicContract) MockHelper.getPublicContractPromiseDetails();
        promise.setTitle(null);
        return promise;
    }

    private static BaseGuaranteeType getReferenceNumberNullPromisePublicContract() {
        PromisePublicContract promise = (PromisePublicContract) MockHelper.getPublicContractPromiseDetails();
        promise.setContractDescription(null);
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateNullPromisePublicContract() {
        PromisePublicContract promise = (PromisePublicContract) MockHelper.getPublicContractPromiseDetails();
        promise.setPromiseEndDate(null);
        return promise;
    }

    private static BaseGuaranteeType getPromiseEndDateInvalidPromisePublicContract() {
        PromisePublicContract promise = (PromisePublicContract) MockHelper.getPublicContractPromiseDetails();
        promise.setPromiseEndDate(LocalDate.now().plusMonths(ABSTRACT_PROMISE_SYSTEM_DATE_EXTRA_MONTHS + 4L));
        return promise;
    }

    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }

}
