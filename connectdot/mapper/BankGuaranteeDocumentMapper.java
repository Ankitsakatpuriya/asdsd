package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.*;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

import static com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotUtils.unsupportedOperation;
import static com.ing.bankguarantees.utils.CommonUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankGuaranteeDocumentMapper {

    private final CurrencyDetailService currencyService;
    private final ConnectDotUtils connectDotUtils;
    private final ConnectDotProperties connectDotProperties;

    public BaseDocumentPayload mapToBankGuaranteePayload(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [mapToBankGuaranteePayload] call");
        BankGuaranteeCode bankGuaranteeCode = connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode();
        return switch (bankGuaranteeCode) {
            case RENTAL -> mapToRentalPayload(connectDotInput);
            case BID_BOND -> mapToBidBondPayload(connectDotInput);
            case ADVANCE_PAYMENT -> mapToAdvancePaymentPayload(connectDotInput);
            case PAYMENT_GUARANTEE -> mapToPaymentGuaranteePayload(connectDotInput);
            case PUBLIC_CONTRACT -> mapToPublicContract(connectDotInput);
            case PERFORMANCE_BOND -> mapToPerformanceBondPayload(connectDotInput);
            case STATE_LOTTERY -> mapToStateLottery(connectDotInput);
            case MONEY_RETENTION_BOND -> mapToMoneyRetentionBondPayload(connectDotInput);
            case OVAM -> mapToOvam(connectDotInput);
            case REAL_ESTATE -> mapToRealEstate(connectDotInput);
            case CUSTOM_1 -> mapToCustomOne(connectDotInput);
            case CUSTOM_2 -> mapToCustomTwo(connectDotInput);
            case CUSTOM_4 -> mapToCustomFour(connectDotInput);
            case CUSTOM_5 -> mapToCustomFive(connectDotInput);
            case DCK_CDC -> unsupportedOperation(bankGuaranteeCode);
            case WOODS_PROM_A -> mapToStandardPromise(connectDotInput);
            case WOODS_PROM_B -> mapToBlankPromise(connectDotInput);
            case WOODS_PROM_VLA -> mapToFlandersStandardPromise(connectDotInput);
            case WOODS_BGWAL_PUBLIC -> mapToWoodsWalloniaPublicEntity(connectDotInput);
            case WOODS_BG_PRIVATE -> mapToWoodsWalloniaPrivateSale(connectDotInput);
            case WOODS_BG_DISCHARGE -> mapToWoodsWalloniaDischarge(connectDotInput);
            case WOODS_BGVLA_PUBLIC -> mapToWoodsVLAPublic(connectDotInput);
            case ABSTRACT_PROM -> mapToAbstractPromise(connectDotInput);
            case PUBLIC_CONTRACT_PROM -> mapToPublicContractPromise(connectDotInput);
            case GOODS_TRANSPORT -> mapToGoodsTransport(connectDotInput);
            case PASSENGER_TRANSPORT -> mapToPassengerTransport(connectDotInput);
            case OPERATORS_TRANSPORT -> mapTransportOperator(connectDotInput);
            case CUSTOMIZED_TEXT -> unsupportedOperation(bankGuaranteeCode);
        };

    }

    private BaseDocumentPayload mapToBidBondPayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToBidBondPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        BidBond bidBond = (BidBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return BidBondDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .expiryCode(bidBond.getBankGuaranteeEndType().name())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(bidBond.getContractDescription())
                .contractReferenceNumber(bidBond.getReferenceNumber())
                .bgExpiry(ConnectDotUtils.getSpecifiedMaturityText(bidBond.getBankGuaranteeEndType(), bidBond.getBankGuaranteeEnd()))
                .bgMaturityDate(bidBond.getMaturityDate())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToMoneyRetentionBondPayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToMoneyRetentionBondPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        MoneyRetentionBond moneyRetentionBond = (MoneyRetentionBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return MoneyRetentionBondDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .contractReferenceNumber(moneyRetentionBond.getReferenceNumber())
                .expiryCode(moneyRetentionBond.getBankGuaranteeEndType().name())
                .contractDescription(moneyRetentionBond.getContractDescription())
                .bankAccountCredit(CommonUtils.formatIbanNumber(moneyRetentionBond.getAdvancePaymentIBAN()))
                .bgExpiry(ConnectDotUtils.getSpecifiedMaturityText(moneyRetentionBond.getBankGuaranteeEndType(), moneyRetentionBond.getBankGuaranteeEnd()))
                .bgMaturityDate(moneyRetentionBond.getMaturityDate())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToPaymentGuaranteePayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToPaymentGuaranteePayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Payment payment = (Payment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return PaymentGuaranteeDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .expiryCode(payment.getBankGuaranteeEndType().name())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .contractReferenceNumber(payment.getReferenceNumber())
                .contractDescription(payment.getContractDescription())
                .bgExpiry(ConnectDotUtils.getSpecifiedMaturityText(payment.getBankGuaranteeEndType(), payment.getBankGuaranteeEnd()))
                .bgMaturityDate(payment.getMaturityDate())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToAdvancePaymentPayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToAdvancePaymentPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        AdvancePayment advancePayment = (AdvancePayment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return AdvancePaymentDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .expiryCode(advancePayment.getBankGuaranteeEndType().name())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractReferenceNumber(advancePayment.getReferenceNumber())
                .contractDescription(advancePayment.getContractDescription())
                .bgExpiry(ConnectDotUtils.getSpecifiedMaturityText(advancePayment.getBankGuaranteeEndType(), advancePayment.getBankGuaranteeEnd()))
                .bgMaturityDate(advancePayment.getMaturityDate())
                .bankAccountCredit(CommonUtils.formatIbanNumber(advancePayment.getAdvancePaymentIBAN()))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToPerformanceBondPayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToPerformanceBondPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PerformanceBond performanceBond = (PerformanceBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Double roundedDown = ConnectDotUtils.getBgAmountFiftyPercentDown(guaranteeDetails.getBgAmount());
        Double roundedUp = ConnectDotUtils.getBgAmountFiftyPercentUp(guaranteeDetails.getBgAmount());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return PerformanceBondDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(performanceBond.getContractDescription())
                .contractReferenceNumber(performanceBond.getReferenceNumber())
                .contractEndDate(performanceBond.getImmediateMaturityDate())
                .expiryCode(performanceBond.getBankGuaranteeEndType().name())
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .bgFiftyPercentAmountRoundedDown(roundedDown)
                .bgFiftyPercentAmountRoundedUp(roundedUp)
                .bgExpiry(ConnectDotUtils.getSpecifiedMaturityText(performanceBond.getBankGuaranteeEndType(), performanceBond.getBankGuaranteeEnd()))
                .bgMaturityDate(ConnectDotUtils.getPerformanceBondMaturityDate(performanceBond))
                .amountInLetterRoundedDown(roundedDown)
                .amountInLetterRoundedUp(roundedUp)
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToRentalPayload(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToRentalPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Rental rental = (Rental) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Integer rentalGracePeriod = CommonUtils.getRentalGracePeriod(rental.getGracePeriod());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return RentalDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .city(rental.getCity())
                .gracePeriod(String.valueOf(rentalGracePeriod))
                .dateOfSignature(rental.getDateOfSignature())
                .street(rental.getStreet())
                .postalCode(rental.getPostalCode())
                .contractEndDate(rental.getContractEndDate())
                .rentalContractEndDate(rental.getContractEndDate().plusDays(rentalGracePeriod))
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }


    private BaseDocumentPayload mapToPublicContract(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToPublicContract] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PublicContract publicContract = (PublicContract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return PublicContractDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .contractReferenceNumber(publicContract.getReferenceNumber())
                .grantDate(publicContract.getGrantDate())
                .contractDescription(publicContract.getTitle())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .totalAmount(publicContract.getTotalAmount().doubleValue())
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToRealEstate(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToRealEstate] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return RealEstateDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToCustomOne(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToCustomOne] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeOne customTypeOne = (CustomTypeOne) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return CustomOneDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountPercentage(String.valueOf(customTypeOne.getAmountPercentage()))
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToCustomTwo(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToCustomTwo] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());

        return CustomTwoDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToCustomFour(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToCustomFour] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());

        return CustomFourDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToCustomFive(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToCustomFive] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());

        return CustomFiveDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToStateLottery(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToStateLottery] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        StateLottery stateLottery = (StateLottery) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return StateLotteryDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .modelType("LOTTO")
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .expiryCode(stateLottery.getBankGuaranteeEndType().name())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractSignDate(stateLottery.getDateOfAgreeInPrinc())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToOvam(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToOvam] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Ovam ovam = (Ovam) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return OvamDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractReferenceNumber(ovam.getReferenceNumber())
                .expiryDate(ovam.getContractDueDate())
                .expiryCode(ovam.getBankGuaranteeEndType().name())
                .dateOfAppointment(ovam.getContractValidityEndDate())
                .contractStartDate(ovam.getWasteTransportStartDate())
                .contractEndDate(ovam.getWasteTransportEndDate())
                .bgExpiry(ovam.getBankGuaranteeEnd())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }


    private BaseDocumentPayload mapToDck(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToDck] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Dck dck = (Dck) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return DckDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(dck.getContractDescription())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToStandardPromise(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToStandardPromise] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWalloniaStandardPromise woodsWalloniaStandardPromise = (WoodsWalloniaStandardPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return StandardPromiseDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .woodContractEndDateA(woodsWalloniaStandardPromise.getPromiseEndDate())
                .woodContractEndDateB(woodsWalloniaStandardPromise.getPromiseEndDate())
                .contractCity(woodsWalloniaStandardPromise.getSalePlace())
                .contractSignDate(woodsWalloniaStandardPromise.getSaleDate())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToFlandersStandardPromise(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToFlandersStandardPromise] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsFlandersStandardPromise woodsFlandersStandardPromise = (WoodsFlandersStandardPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return StandardPromiseFlandersDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractCity(woodsFlandersStandardPromise.getSalePlace())
                .contractSignDate(woodsFlandersStandardPromise.getSaleDate())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToBlankPromise(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToBlankPromise] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsBlankPromise woodsBlankPromise = (WoodsBlankPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return WoodsBlankPromisePayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractEndDate(woodsBlankPromise.getPromiseEndDate())
                .woodContractEndDateB(woodsBlankPromise.getPromiseEndDate())
                .lotsDescription(woodsBlankPromise.getLotsDescription())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }


    private BaseDocumentPayload mapToWoodsWalloniaPublicEntity(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToWoodsWalloniaPublicEntity] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWALPublic woodsWALPublic = (WoodsWALPublic) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        String bgCurrencyCode = currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency());
        return WoodsPublicEntityDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(bgCurrencyCode)
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .woodMinAmount(CommonUtils.calculateWoodsRemainingAmount(woodsWALPublic.getSalePrice()).doubleValue())
                .cash(ObjectUtils.isNotEmpty(woodsWALPublic.getCash()) ? woodsWALPublic.getCash().doubleValue() : null)
                .trancheOne(woodsWALPublic.getFirstTransactionAmt().doubleValue())
                .deadlineOne(woodsWALPublic.getFirstDeadline())
                .trancheTwo(woodsWALPublic.getSecondTransactionAmt().doubleValue())
                .deadlineTwo(woodsWALPublic.getSecondDeadline())
                .contractCity(woodsWALPublic.getSalePlace())
                .contractSignDate(woodsWALPublic.getSaleDate())
                .contractDescription(woodsWALPublic.getLotsDescription())
                .salePrice(woodsWALPublic.getSalePrice().doubleValue())
                .replacePromiseId(woodsWALPublic.getPromiseIds())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .deadlineFour(woodsWALPublic.getFourthDeadline())
                .deadlineThree(woodsWALPublic.getThirdDeadline())
                .trancheThree(ObjectUtils.isNotEmpty(woodsWALPublic.getThirdTransactionAmt()) ? woodsWALPublic.getThirdTransactionAmt().doubleValue() : null)
                .trancheFour(ObjectUtils.isNotEmpty(woodsWALPublic.getFourthTransactionAmt()) ? woodsWALPublic.getFourthTransactionAmt().doubleValue() : null)
                .build();
    }

    private BaseDocumentPayload mapToWoodsWalloniaDischarge(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToWoodsWalloniaDischarge] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsDischarge woodsDischarge = (WoodsDischarge) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        String bgCurrencyCode = currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency());
        return WoodsDischargeDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(bgCurrencyCode)
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractCity(woodsDischarge.getSalePlace())
                .contractSignDate(woodsDischarge.getSaleDate())
                .contractDescription(woodsDischarge.getLotsDescription())
                .salePrice(woodsDischarge.getSalePrice().doubleValue())
                .replacePromiseId(woodsDischarge.getPromiseIds())
                .woodMinAmount(CommonUtils.calculateWoodsRemainingAmount(woodsDischarge.getSalePrice()).doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToWoodsWalloniaPrivateSale(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToWoodsWalloniaPrivateSale] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsPrivate woodsPrivate = (WoodsPrivate) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return WoodsPrivateSaleDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(woodsPrivate.getLotsDescription())
                .contractCity(woodsPrivate.getSalePlace())
                .contractSignDate(woodsPrivate.getSaleDate())
                .salePrice(woodsPrivate.getSalePrice() != null ? woodsPrivate.getSalePrice().doubleValue() : null)
                .trancheOne(woodsPrivate.getFirstTransactionAmt().doubleValue())
                .deadlineOne(woodsPrivate.getFirstDeadline())
                .trancheTwo(woodsPrivate.getSecondTransactionAmt().doubleValue())
                .deadlineTwo(woodsPrivate.getSecondDeadline())
                .woodValidityEndDate(woodsPrivate.getMaturityDate())
                .replacePromiseId(woodsPrivate.getPromiseIds())
                .expiryCode(woodsPrivate.getBankGuaranteeEndType().name())
                .bgMaturityDate(woodsPrivate.getMaturityDate())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .deadlineThree(woodsPrivate.getThirdDeadline())
                .deadlineFour(woodsPrivate.getFourthDeadline())
                .woodMinAmount(ObjectUtils.isNotEmpty(woodsPrivate.getResidualAmount()) ? woodsPrivate.getResidualAmount().doubleValue() : null)
                .trancheThree(ObjectUtils.isNotEmpty(woodsPrivate.getThirdTransactionAmt()) ? woodsPrivate.getThirdTransactionAmt().doubleValue() : null)
                .trancheFour(ObjectUtils.isNotEmpty(woodsPrivate.getFourthTransactionAmt()) ? woodsPrivate.getFourthTransactionAmt().doubleValue() : null)
                .build();
    }

    private BaseDocumentPayload mapToWoodsVLAPublic(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToWoodsVLAPublic] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsVLAPublic woodsVLAPublic = (WoodsVLAPublic) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return WoodsVLADocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .trancheOne(woodsVLAPublic.getFirstTransactionAmt().doubleValue())
                .deadlineOne(woodsVLAPublic.getFirstDeadline())
                .trancheTwo(woodsVLAPublic.getSecondTransactionAmt().doubleValue())
                .deadlineTwo(woodsVLAPublic.getSecondDeadline())
                .contractCity(woodsVLAPublic.getSalePlace())
                .contractSignDate(woodsVLAPublic.getSaleDate())
                .replacePromiseId(woodsVLAPublic.getPromiseIds())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .deadlineThree(woodsVLAPublic.getThirdDeadline())
                .deadlineFour(woodsVLAPublic.getFourthDeadline())
                .trancheThree(ObjectUtils.isNotEmpty(woodsVLAPublic.getThirdTransactionAmt()) ? woodsVLAPublic.getThirdTransactionAmt().doubleValue() : null)
                .trancheFour(ObjectUtils.isNotEmpty(woodsVLAPublic.getFourthTransactionAmt()) ? woodsVLAPublic.getFourthTransactionAmt().doubleValue() : null)
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToAbstractPromise(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToAbstractPromise] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PromiseAbstract promiseAbstract = (PromiseAbstract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return PromiseAbstractDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(promiseAbstract.getContractDescription())
                .contractReference(promiseAbstract.getReferenceNumber())
                .promiseEndDate(promiseAbstract.getPromiseEndDate())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();
    }

    private BaseDocumentPayload mapToPublicContractPromise(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToPublicContractPromise] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PromisePublicContract promisePublicContract = (PromisePublicContract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return PromisePublicContractDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .contractDescription(promisePublicContract.getTitle())
                .contractReference(promisePublicContract.getContractDescription())
                .promiseEndDate(promisePublicContract.getPromiseEndDate())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }

    private BaseDocumentPayload mapToGoodsTransport(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToGoodsTransport] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        GoodsTransport goodsTransport = (GoodsTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        boolean isTransportAmendCase = checkTransportAmendCase(bankGuaranteeRequestData.getGuaranteeDetails());
        return GoodsTransportDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(isTransportAmendCase ? goodsTransport.getBankGuaranteeId()
                        : ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .licenseNumber(goodsTransport.getLicenseNumber())
                .contractAmount(isTransportAmendCase ? 0 : guaranteeDetails.getBgAmount().doubleValue())
                .typeAmount(getTransportTypeAmount(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(), guaranteeDetails, connectDotProperties.getTypeAmount()))
                .build();
    }

    private BaseDocumentPayload mapToPassengerTransport(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapToPassengerTransport] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PassengerTransport passengerTransport = (PassengerTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        boolean isTransportAmendCase = checkTransportAmendCase(bankGuaranteeRequestData.getGuaranteeDetails());
        return PassengerTransportDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(isTransportAmendCase ? passengerTransport.getBankGuaranteeId() : ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .licenseNumber(passengerTransport.getLicenseNumber())
                .contractAmount(isTransportAmendCase ? 0 : guaranteeDetails.getBgAmount().doubleValue())
                .typeAmount(getTransportTypeAmount(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(), guaranteeDetails, connectDotProperties.getTypeAmount()))
                .build();
    }


    private BaseDocumentPayload mapTransportOperator(ConnectDotInput connectDotInput) {
        log.info("BankGuaranteeDocumentMapper [mapTransportOperator] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        OperatorsTransport operatorsTransport = (OperatorsTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        Locale bgLanguageLocale = new Locale(guaranteeDetails.getBgLanguage().getLanguageCode());
        return OperatorTransportDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().name())
                .printStatus(connectDotInput.getDocumentType().getName())
                .bgAmount(guaranteeDetails.getBgAmount().doubleValue())
                .bgCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(bgLanguageLocale, bgLanguageLocale))
                .amountInLetter(guaranteeDetails.getBgAmount().doubleValue())
                .firstSignerName(connectDotProperties.getFirstIngSignerName())
                .secondSignerName(connectDotProperties.getSecondIngSignerName())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .profession(operatorsTransport.getTransportOperatorActivityType() != null
                        ? getTransportOperatorTranslation(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                        operatorsTransport.getTransportOperatorActivityType())
                        : "N.A")
                .publicationBelgianGazetteFR(connectDotProperties.getPublicationBelgianGazetteFR())
                .publicationBelgianGazetteNL(connectDotProperties.getPublicationBelgianGazetteNL())
                .build();
    }
}
