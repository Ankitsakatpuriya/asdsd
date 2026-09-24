package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.CreditType;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BaseDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.BankGuaranteeDetailPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.CollateralPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.ContractDetailPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload.CovenantPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.GuaranteeInformationDto;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput.Collateral;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.RENTAL;
import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.WOODS_PROM_B;
import static com.ing.bankguarantees.utils.CommonUtils.getTransportFodGuaranteeType;
import static com.ing.bankguarantees.utils.CommonUtils.getTransportOperatorTranslation;
import static com.ing.bankguarantees.utils.ConstantUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDocumentMapper {

    private final CurrencyDetailService currencyService;
    private final ConnectDotUtils connectDotUtils;
    private final ConnectDotProperties connectDotProperties;
    private final CountryDetailService countryService;

    @Value("${bgos.default.currencies.3}")
    private String currencyCode;

    public BaseDocumentPayload mapToContractPayload(ConnectDotInput connectDotInput) {
        log.info("ContractDocumentMapper [mapToContractPayload] call");
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        GuaranteeInformationDto guaranteeInformationDto = retrieveGuaranteeInformation(bankGuaranteeRequestData);
        return ContractDocumentPayload.builder()
                .date(LocalDate.now())
                .modelType(ConnectDotUtils.getModelType(bgCode))
                .printStatus(connectDotInput.getDocumentType().getName())
                .creditType(connectDotInput.getCreditType())
                .bankAccountBooking(ConnectDotUtils.getCreditLineAccountNumber(bankGuaranteeRequestData))
                .bankAccountDebit(CommonUtils.formatIbanNumber(bankGuaranteeRequestData.getFinancialInformation().getAccountToBeDebited().getIbanNumber()))
                .bankAccountCredit(guaranteeInformationDto.getBankAccountCredit())
                .applicant(connectDotUtils.prepareApplicant(connectDotInput))
                .beneficiary(connectDotUtils.prepareBeneficiary(connectDotInput))
                .bankGuaranteeDetails(prepareBankGuaranteeDetails(bankGuaranteeRequestData, guaranteeInformationDto))
                .contractDetails(guaranteeInformationDto.getContractDetailPayload())
                .representedBy(prepareRepresentedBy(bankGuaranteeRequestData))
                .collateralPayloadList(prepareCollaterals(connectDotInput))
                .covenantPayloadList(prepareCovenants(connectDotInput))
                .periodicity(getContractPeriodicity(bankGuaranteeRequestData.getGuaranteeDetails().getBgCode(), connectDotProperties, bankGuaranteeRequestData.getTranslationLanguage()))
                .ratePerYear(connectDotProperties.getRatePerYear())
                .minimumPerRecord(bgCode == BankGuaranteeCode.DCK_CDC
                        ? Double.valueOf(connectDotProperties.getMinimumPerRecordForDck())
                        : Double.valueOf(connectDotProperties.getMinimumPerRecord()))
                .bgLanguage(ConnectDotUtils.getDisplayLanguage(new Locale(bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode()),
                        bankGuaranteeRequestData.getTranslationLanguage()))
                .referenceNumber(ConnectDotUtils.getBankGuaranteeReference(connectDotInput))
                .build();

    }

    public String getContractPeriodicity(final BankGuaranteeCode bankGuaranteeCode, ConnectDotProperties connectDotProperties, Locale translationLanguage) {
        return RENTAL == bankGuaranteeCode
                ? getPeriodicityForRental(connectDotProperties, translationLanguage)
                : getPeriodicityForOthers(connectDotProperties, translationLanguage);
    }

    private String getPeriodicityForOthers(ConnectDotProperties connectDotProperties, Locale translationLanguage) {
        return switch (translationLanguage.getLanguage()) {
            case FRENCH -> connectDotProperties.getPeriodicityForOthersFr();
            case DUTCH -> connectDotProperties.getPeriodicityForOthersNl();
            default -> connectDotProperties.getPeriodicityForOthersEn();
        };
    }

    private String getPeriodicityForRental(ConnectDotProperties connectDotProperties, Locale translationLanguage) {
        return switch (translationLanguage.getLanguage()) {
            case FRENCH -> connectDotProperties.getPeriodicityForRentalFr();
            case DUTCH -> connectDotProperties.getPeriodicityForRentalNl();
            default -> connectDotProperties.getPeriodicityForRentalEn();
        };
    }

    private GuaranteeInformationDto retrieveGuaranteeInformation(BankGuaranteeRequestData bankGuaranteeRequestData) {

        log.info("ConnectDotMapper [prepareContractDetails] call");
        return switch (bankGuaranteeRequestData.getGuaranteeDetails().getBgCode()) {
            case RENTAL -> prepareGuaranteeDetailForRental(bankGuaranteeRequestData);
            case STATE_LOTTERY -> prepareGuaranteeDetailForStateLottery(bankGuaranteeRequestData);
            case PUBLIC_CONTRACT -> prepareGuaranteeDetailForPublicContract(bankGuaranteeRequestData);
            case OVAM -> prepareGuaranteeDetailForOvam(bankGuaranteeRequestData);
            case CUSTOM_1 -> prepareGuaranteeDetailForCustomOne(bankGuaranteeRequestData);
            case REAL_ESTATE -> prepareGuaranteeDetailForRealEstate(bankGuaranteeRequestData);
            case CUSTOM_2 -> prepareGuaranteeDetailForCustomTwo(bankGuaranteeRequestData);
            case CUSTOM_5 -> prepareGuaranteeDetailForCustomFive(bankGuaranteeRequestData);
            case CUSTOM_4 -> prepareGuaranteeDetailForCustomFour(bankGuaranteeRequestData);
            case BID_BOND -> prepareGuaranteeDetailForBidBond(bankGuaranteeRequestData);
            case ADVANCE_PAYMENT -> prepareGuaranteeDetailForAdvancePayment(bankGuaranteeRequestData);
            case PAYMENT_GUARANTEE -> prepareGuaranteeDetailForPaymentGuarantee(bankGuaranteeRequestData);
            case PERFORMANCE_BOND -> prepareGuaranteeDetailForPerformanceBond(bankGuaranteeRequestData);
            case MONEY_RETENTION_BOND -> prepareGuaranteeDetailForMoneyRetentionBond(bankGuaranteeRequestData);
            case DCK_CDC -> prepareGuaranteeDetailForDck(bankGuaranteeRequestData);
            case WOODS_PROM_A -> prepareGuaranteeDetailForStandardPromise(bankGuaranteeRequestData);
            case WOODS_PROM_VLA -> prepareGuaranteeDetailForFlandersStandardPromise(bankGuaranteeRequestData);
            case WOODS_PROM_B -> prepareGuaranteeDetailForBlankPromise(bankGuaranteeRequestData);
            case WOODS_BGWAL_PUBLIC -> prepareGuaranteeDetailForWoodsPublicEntity(bankGuaranteeRequestData);
            case WOODS_BG_PRIVATE -> prepareGuaranteeDetailForWoodsPrivateSale(bankGuaranteeRequestData);
            case WOODS_BGVLA_PUBLIC -> prepareGuaranteeDetailForWoodsVLAPublic(bankGuaranteeRequestData);
            case WOODS_BG_DISCHARGE -> prepareGuaranteeDetailForWoodsDischarge(bankGuaranteeRequestData);
            case GOODS_TRANSPORT -> prepareGuaranteeDetailForGoodsTransport(bankGuaranteeRequestData);
            case PASSENGER_TRANSPORT -> prepareGuaranteeDetailForPassengerTransport(bankGuaranteeRequestData);
            case OPERATORS_TRANSPORT -> prepareGuaranteeDetailForOperatorTransport(bankGuaranteeRequestData);
            case ABSTRACT_PROM -> prepareGuaranteeDetailForAbstractPromise(bankGuaranteeRequestData);
            case PUBLIC_CONTRACT_PROM -> prepareGuaranteeDetailForPublicContractPromise(bankGuaranteeRequestData);
            case CUSTOMIZED_TEXT -> prepareGuaranteeDetailForCustomizedText(bankGuaranteeRequestData);
        };

    }


    private GuaranteeInformationDto prepareGuaranteeDetailForBidBond(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForBidBond] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        BidBond bidBond = (BidBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(bidBond.getReferenceNumber())
                        .contractDescription(bidBond.getContractDescription())
                        .expiryCode(bidBond.getBankGuaranteeEndType().name())
                        .bgMaturityDate(bidBond.getMaturityDate())
                        .build())
                .bankGuaranteeDescription(bidBond.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForMoneyRetentionBond(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForMoneyRetentionBond] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        MoneyRetentionBond moneyRetentionBond = (MoneyRetentionBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(moneyRetentionBond.getReferenceNumber())
                        .contractDescription(moneyRetentionBond.getContractDescription())
                        .expiryCode(moneyRetentionBond.getBankGuaranteeEndType().name())
                        .bgMaturityDate(moneyRetentionBond.getMaturityDate())
                        .build())
                .bankGuaranteeDescription(moneyRetentionBond.getBankGuaranteeEnd())
                .bankAccountCredit(CommonUtils.formatIbanNumber(moneyRetentionBond.getAdvancePaymentIBAN()))
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForPaymentGuarantee(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForPaymentGuarantee] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Payment payment = (Payment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(payment.getReferenceNumber())
                        .contractDescription(payment.getContractDescription())
                        .expiryCode(payment.getBankGuaranteeEndType().name())
                        .bgMaturityDate(payment.getMaturityDate())
                        .build())
                .bankGuaranteeDescription(payment.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForAdvancePayment(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForAdvancePayment] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        AdvancePayment advancePayment = (AdvancePayment) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(advancePayment.getReferenceNumber())
                        .contractDescription(advancePayment.getContractDescription())
                        .expiryCode(advancePayment.getBankGuaranteeEndType().name())
                        .bgMaturityDate(advancePayment.getMaturityDate())
                        .build())
                .bankGuaranteeDescription(advancePayment.getBankGuaranteeEnd())
                .bankAccountCredit(CommonUtils.formatIbanNumber(advancePayment.getAdvancePaymentIBAN()))
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForPerformanceBond(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForPerformanceBond] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PerformanceBond performanceBond = (PerformanceBond) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(performanceBond.getReferenceNumber())
                        .contractDescription(performanceBond.getContractDescription())
                        .expiryCode(performanceBond.getBankGuaranteeEndType().name())
                        .bgMaturityDate(ConnectDotUtils.getPerformanceBondMaturityDate(performanceBond))
                        .contractEndDate(performanceBond.getImmediateMaturityDate())
                        .build())
                .bankGuaranteeDescription(performanceBond.getBankGuaranteeEnd())
                .build();

    }

    private ContractDocumentPayload.RepresentPayload prepareRepresentedBy(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareRepresentedBy] call");
        String secondSignerName = null;
        String firstSignerName = null;
        List<LegalRepresentativeData> selectedSigners = CommonUtils.getSelectedSigner(bankGuaranteeRequestData.getLegalRepresentatives());
        if (CollectionUtils.isNotEmpty(selectedSigners)) {
            for (LegalRepresentativeData legalRepresentativeData : selectedSigners) {
                if (legalRepresentativeData.isFirstSigner()) {
                    firstSignerName = legalRepresentativeData.getFullName();
                } else {
                    secondSignerName = legalRepresentativeData.getFullName();
                }
            }
        }

        return ContractDocumentPayload.RepresentPayload.builder()
                .legalRep1(firstSignerName)
                .legalRep2(secondSignerName)
                .build();
    }

    private BankGuaranteeDetailPayload prepareBankGuaranteeDetails(BankGuaranteeRequestData bankGuaranteeRequestData, GuaranteeInformationDto guaranteeInformationDto) {
        log.info("ConnectDotMapper [prepareBankGuaranteeDetails] call");
        double bgAmount = bankGuaranteeRequestData.getGuaranteeDetails().getBgAmount().doubleValue();
        return BankGuaranteeDetailPayload.builder()
                .bgLang(ConnectDotUtils.getDisplayLanguage(new Locale(bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode()),
                        bankGuaranteeRequestData.getTranslationLanguage()))
                .sendTo(getSendTo(bankGuaranteeRequestData))
                .sendVia(bankGuaranteeRequestData.getDeliveryInformation().getMode().getDescription())
                .bgCurrency(currencyService.getCurrencyCodeByValue(bankGuaranteeRequestData.getGuaranteeDetails().getBgCurrency()))
                .bgAmount(bgAmount)
                .contractCreditAmount(getContractCreditAmount(bankGuaranteeRequestData))
                .bgExpiryDescription(guaranteeInformationDto.getBankGuaranteeDescription())
                .build();

    }

    private double getContractCreditAmount(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        double bgAmount = guaranteeDetails.getBgAmount().doubleValue();
        return switch (guaranteeDetails.getBgCode()) {
            case WOODS_BG_PRIVATE -> {
                WoodsPrivate woodsPrivate = (WoodsPrivate) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
                double residualAmount = ObjectUtils.isNotEmpty(woodsPrivate.getResidualAmount()) ? woodsPrivate.getResidualAmount().doubleValue() : 0;
                yield bgAmount + residualAmount;
            }
            case WOODS_BGWAL_PUBLIC -> {
                WoodsWALPublic woodsWALPublic = (WoodsWALPublic) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
                double residualAmount = ObjectUtils.isNotEmpty(woodsWALPublic.getResidualAmount()) ? woodsWALPublic.getResidualAmount().doubleValue() : 0;
                yield bgAmount + residualAmount;
            }
            case WOODS_BG_DISCHARGE -> bgAmount;
            case STATE_LOTTERY -> bgAmount + (bgAmount * 0.1);
            default -> 0;
        };
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForRental(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareContractForRental] call");

        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Rental rental = (Rental) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCity(rental.getCity())
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractStreet(rental.getStreet())
                        .contractZip(rental.getPostalCode())
                        .contractSignDate(rental.getDateOfSignature())
                        .contractEndDate(rental.getContractEndDate())
                        .contractReference(rental.getReferenceNumber())
                        .contractCountry(countryService.getCountryNameByCode(bankGuaranteeRequestData.getTranslationLanguage(), rental.getCountry()))
                        .bgMaturityDate(rental.getRentalEndDate())
                        .build())
                .bankGuaranteeDescription(rental.getExpiryDate())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForPublicContract(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareContractFoPublicContract] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PublicContract publicContract = (PublicContract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractSignDate(publicContract.getGrantDate())
                        .contractAmount(publicContract.getTotalAmount().doubleValue())
                        .contractReference(publicContract.getReferenceNumber())
                        .contractDescription(publicContract.getTitle())
                        .build())
                .bankGuaranteeDescription(publicContract.getExpiryDate())
                .build();


    }

    private GuaranteeInformationDto prepareGuaranteeDetailForCustomOne(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareContractForCustomOne] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeOne customTypeOne = (CustomTypeOne) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .amountPercentage(String.valueOf(customTypeOne.getAmountPercentage()))
                        .build())
                .bankGuaranteeDescription(customTypeOne.getBankGuaranteeEnd())
                .build();


    }

    private GuaranteeInformationDto prepareGuaranteeDetailForCustomFour(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareContractForCustomFour] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeFour customTypeFour = (CustomTypeFour) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractDescription(customTypeFour.getContractDescription())
                        .build())
                .bankGuaranteeDescription(customTypeFour.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForOvam(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForOvam] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Ovam ovam = (Ovam) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractSignDate(ovam.getWasteTransportStartDate())
                        .contractReference(ovam.getReferenceNumber())
                        .contractEndDate(ovam.getWasteTransportEndDate())
                        .expiryCode(ovam.getBankGuaranteeEndType().name())
                        .bgMaturityDate(ovam.getContractValidityEndDate())
                        .expiryCode(ovam.getBankGuaranteeEndType().name())
                        .contractDueDate(ovam.getContractDueDate())
                        .contractValidityEndDate(ovam.getContractValidityEndDate())
                        .build())
                .bankGuaranteeDescription(ovam.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForCustomTwo(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForCustomTwo] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeTwo customTypeTwo = (CustomTypeTwo) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .bankGuaranteeDescription(customTypeTwo.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForRealEstate(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForRealEstate] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        RealEstate realEstate = (RealEstate) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .bankGuaranteeDescription(realEstate.getBankGuaranteeEnd())
                .build();

    }

    private GuaranteeInformationDto prepareGuaranteeDetailForCustomFive(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForCustomFive] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomTypeFive customTypeFive = (CustomTypeFive) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .bankGuaranteeDescription(customTypeFive.getBankGuaranteeEnd())
                .build();

    }


    private GuaranteeInformationDto prepareGuaranteeDetailForStateLottery(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareContractForStateLottery] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        StateLottery stateLottery = (StateLottery) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());

        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractSignDate(stateLottery.getDateOfAgreeInPrinc())
                        .build())
                .bankGuaranteeDescription(stateLottery.getBankGuaranteeEnd())
                .build();

    }


    private List<CollateralPayload> prepareCollaterals(ConnectDotInput connectDotInput) {

        log.info("ConnectDotMapper [prepareCollaterals] call");

        List<CollateralPayload> collateralPayloadList = new ArrayList<>();
        if (CreditType.ISOLATED == connectDotInput.getCreditType() && ObjectUtils.isNotEmpty(connectDotInput.getGarOutput())) {
            if (CollectionUtils.isEmpty(connectDotInput.getGarOutput().getCollaterals())) {
                collateralPayloadList.add(CollateralPayload.builder()
                        .collateralType("NIHIL")
                        .build());
            } else {
                for (Collateral collateral : connectDotInput.getGarOutput().getCollaterals()) {
                    collateralPayloadList.add(CollateralPayload.builder()
                            .collateralValue(collateral.getGarAmount().getValue())
                            .currency(collateral.getGarAmount().getCurrency())
                            .coverRegistrationDate(collateral.getDate())
                            .collateralType(collateral.getCollateralType())
                            .grantedBy(transformGrantedByNames(collateral.getGrantedBy()))
                            .comments(collateral.getComments())
                            .assetDescription(collateral.getAssertDescription())
                            .generalCoverType(collateral.getGeneralCoverType())
                            .build());
                }
            }
        }
        return collateralPayloadList;

    }


    private List<CovenantPayload> prepareCovenants(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [prepareCovenants] call");
        List<CovenantPayload> covenantPayloadList = new ArrayList<>();
        if (CreditType.ISOLATED == connectDotInput.getCreditType() && ObjectUtils.isNotEmpty(connectDotInput.getGarOutput())) {
            if (CollectionUtils.isEmpty(connectDotInput.getGarOutput().getCovenants())) {
                covenantPayloadList.add(CovenantPayload.builder()
                        .covenants("NIHIL")
                        .build());
            } else {
                for (GarOutput.Covenant covenant : connectDotInput.getGarOutput().getCovenants()) {
                    covenantPayloadList.add(CovenantPayload.builder()
                            .covenants(covenant.getAgreementType())
                            .coverRegistrationDate(covenant.getDate())
                            .grantedBy(transformGrantedByNames(covenant.getGrantedBy()))
                            .comments(covenant.getComments())
                            .generalCoverType(covenant.getGeneralCoverType())
                            .build());
                }
            }
        }
        return covenantPayloadList;

    }

    private String getSendTo(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [getSendTo] call");
        return switch (bankGuaranteeRequestData.getDeliveryInformation().getRecipient()) {
            case ME -> bankGuaranteeRequestData.isIssueToAnotherParty() ?
                    bankGuaranteeRequestData.getApplicant().getOrganisationName().getFullName()
                    : bankGuaranteeRequestData.getInstructingParty().getOrganisation().getOrganisationName().getFullName();
            case BENEFICIARY -> bankGuaranteeRequestData.getGuaranteeDetails().getBgCode() != WOODS_PROM_B
                    ? CommonUtils.getBeneficiaryName(bankGuaranteeRequestData.getBeneficiary())
                    : null;

        };
    }

    private String transformGrantedByNames(final List<Collateral.GarParty> names) {
        return names.isEmpty() ? null : names.stream().map(Collateral.GarParty::getName)
                .collect(Collectors.joining(","));
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForDck(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForDck] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        Dck dck = (Dck) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractDescription(dck.getContractDescription())
                        .contractCity(dck.getBuildingAddress().getCity())
                        .contractStreet(dck.getBuildingAddress().getStreet())
                        .contractZip(dck.getBuildingAddress().getPostalCode())
                        .contractType(dck.getContractDescription())
                        .build())
                .bankGuaranteeDescription(dck.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForStandardPromise(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForStandardPromise] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWalloniaStandardPromise woodsWalloniaStandardPromise = (WoodsWalloniaStandardPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .woodContractEndDateA(woodsWalloniaStandardPromise.getPromiseEndDate())
                        .contractCity(woodsWalloniaStandardPromise.getSalePlace())
                        .contractSignDate(woodsWalloniaStandardPromise.getSaleDate())
                        .build())
                .bankGuaranteeDescription(woodsWalloniaStandardPromise.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForBlankPromise(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForBlankPromise] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsBlankPromise woodsBlankPromise = (WoodsBlankPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .woodContractEndDateB(woodsBlankPromise.getPromiseEndDate())
                        .lotsDescription(woodsBlankPromise.getLotsDescription())
                        .build())
                .bankGuaranteeDescription(woodsBlankPromise.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForFlandersStandardPromise(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForFlandersStandardPromise] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsFlandersStandardPromise woodsFlandersStandardPromise = (WoodsFlandersStandardPromise) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .woodContractEndDateA(woodsFlandersStandardPromise.getPromiseEndDate())
                        .contractCity(woodsFlandersStandardPromise.getSalePlace())
                        .contractSignDate(woodsFlandersStandardPromise.getSaleDate())
                        .build())
                .bankGuaranteeDescription(woodsFlandersStandardPromise.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForWoodsPublicEntity(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForWoodsPublicEntity] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsWALPublic woodsWALPublic = (WoodsWALPublic) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractDescription(woodsWALPublic.getLotsDescription())
                        .contractSignDate(woodsWALPublic.getSaleDate())
                        .contractCity(woodsWALPublic.getSalePlace())
                        .remainingAmount(ObjectUtils.isNotEmpty(woodsWALPublic.getResidualAmount()) ? woodsWALPublic.getResidualAmount().doubleValue() : null)
                        .contractAmount(woodsWALPublic.getSalePrice() != null ? woodsWALPublic.getSalePrice().doubleValue() : null)
                        .contractCurrency(currencyCode)
                        .cash(ObjectUtils.isNotEmpty(woodsWALPublic.getCash()) ? woodsWALPublic.getCash().doubleValue() : null)
                        .trancheOne(woodsWALPublic.getFirstTransactionAmt().doubleValue())
                        .trancheTwo(woodsWALPublic.getSecondTransactionAmt().doubleValue())
                        .trancheThree(ObjectUtils.isNotEmpty(woodsWALPublic.getThirdTransactionAmt()) ? woodsWALPublic.getThirdTransactionAmt().doubleValue() : null)
                        .trancheFour(ObjectUtils.isNotEmpty(woodsWALPublic.getFourthTransactionAmt()) ? woodsWALPublic.getFourthTransactionAmt().doubleValue() : null)
                        .deadlineOne(woodsWALPublic.getFirstDeadline())
                        .deadlineTwo(woodsWALPublic.getSecondDeadline())
                        .deadlineThree(woodsWALPublic.getThirdDeadline())
                        .deadlineFour(woodsWALPublic.getFourthDeadline())
                        .replacePromiseId(woodsWALPublic.getPromiseIds())
                        .build())
                .bankGuaranteeDescription(woodsWALPublic.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForWoodsDischarge(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForWoodsDischarge] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsDischarge woodsDischarge = (WoodsDischarge) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractDescription(woodsDischarge.getLotsDescription())
                        .contractSignDate(woodsDischarge.getSaleDate())
                        .contractCity(woodsDischarge.getSalePlace())
                        .replacePromiseId(woodsDischarge.getPromiseIds())
                        .contractCurrency(currencyCode)
                        .contractAmount(woodsDischarge.getSalePrice() != null ? woodsDischarge.getSalePrice().doubleValue() : null)
                        .build())
                .bankGuaranteeDescription(woodsDischarge.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForWoodsPrivateSale(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForWoodsPrivateSale] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsPrivate woodsPrivate = (WoodsPrivate) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .cash(woodsPrivate.getCash() != null ? woodsPrivate.getCash().doubleValue() : null)
                        .remainingAmount(ObjectUtils.isNotEmpty(woodsPrivate.getResidualAmount()) ? woodsPrivate.getResidualAmount().doubleValue() : null)
                        .trancheOne(woodsPrivate.getFirstTransactionAmt().doubleValue())
                        .trancheTwo(woodsPrivate.getSecondTransactionAmt().doubleValue())
                        .trancheThree(ObjectUtils.isNotEmpty(woodsPrivate.getThirdTransactionAmt()) ? woodsPrivate.getThirdTransactionAmt().doubleValue() : null)
                        .trancheFour(ObjectUtils.isNotEmpty(woodsPrivate.getFourthTransactionAmt()) ? woodsPrivate.getFourthTransactionAmt().doubleValue() : null)
                        .deadlineOne(woodsPrivate.getFirstDeadline())
                        .deadlineTwo(woodsPrivate.getSecondDeadline())
                        .deadlineThree(woodsPrivate.getThirdDeadline())
                        .deadlineFour(woodsPrivate.getFourthDeadline())
                        .woodValidityEndDate(woodsPrivate.getMaturityDate())
                        .contractDescription(woodsPrivate.getLotsDescription())
                        .contractSignDate(woodsPrivate.getSaleDate())
                        .contractCity(woodsPrivate.getSalePlace())
                        .contractCountry(currencyCode)
                        .contractAmount(woodsPrivate.getSalePrice() != null ? woodsPrivate.getSalePrice().doubleValue() : null)
                        .build())
                .bankGuaranteeDescription(woodsPrivate.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForWoodsVLAPublic(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForWoodsVLAPublic] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        WoodsVLAPublic woodsVLAPublic = (WoodsVLAPublic) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractSignDate(woodsVLAPublic.getSaleDate())
                        .contractCity(woodsVLAPublic.getSalePlace())
                        .cash(woodsVLAPublic.getCash() != null ? woodsVLAPublic.getCash().doubleValue() : null)
                        .trancheOne(woodsVLAPublic.getFirstTransactionAmt().doubleValue())
                        .trancheTwo(woodsVLAPublic.getSecondTransactionAmt().doubleValue())
                        .replacePromiseId(woodsVLAPublic.getPromiseIds())
                        .trancheThree(woodsVLAPublic.getThirdTransactionAmt() != null ? woodsVLAPublic.getThirdTransactionAmt().doubleValue() : null)
                        .trancheFour(woodsVLAPublic.getFourthTransactionAmt() != null ? woodsVLAPublic.getFourthTransactionAmt().doubleValue() : null)
                        .deadlineOne(woodsVLAPublic.getFirstDeadline())
                        .deadlineTwo(woodsVLAPublic.getSecondDeadline())
                        .deadlineThree(woodsVLAPublic.getThirdDeadline())
                        .deadlineFour(woodsVLAPublic.getFourthDeadline())
                        .build())
                .bankGuaranteeDescription(woodsVLAPublic.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForGoodsTransport(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForGoodsTransport] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        GoodsTransport goodsTransport = (GoodsTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .numberOfVehicles(String.valueOf(goodsTransport.getNoOfVehicle()))
                        .licenseNumber(String.valueOf(goodsTransport.getNoOfVehicle()))
                        .fodGuarantee(goodsTransport.getFodGuarantee() != null
                                ? getTransportFodGuaranteeType(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                                goodsTransport.getFodGuarantee())
                                : "N.A")
                        .license(goodsTransport.getLicenseNumber())
                        .bgReferenceNumber(goodsTransport.getBankGuaranteeId())
                        .contractAmount(guaranteeDetails.getBgAmount().doubleValue())
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .bankGuaranteeDescription(goodsTransport.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForOperatorTransport(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForOperatorTransport] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        OperatorsTransport operatorsTransport = (OperatorsTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .numberOfVehicles(String.valueOf(operatorsTransport.getNoOfVehicle()))
                        .licenseNumber(String.valueOf(operatorsTransport.getNoOfVehicle()))
                        .contractAmount(guaranteeDetails.getBgAmount().doubleValue())
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .profession(operatorsTransport.getTransportOperatorActivityType() != null
                                ? getTransportOperatorTranslation(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                                operatorsTransport.getTransportOperatorActivityType())
                                : "N.A")
                        .build())
                .bankGuaranteeDescription(operatorsTransport.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForPassengerTransport(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForPassengerTransport] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PassengerTransport passengerTransport = (PassengerTransport) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .numberOfVehicles(String.valueOf(passengerTransport.getNoOfVehicle()))
                        .licenseNumber(String.valueOf(passengerTransport.getNoOfVehicle()))
                        .fodGuarantee(passengerTransport.getFodGuarantee() != null
                                ? getTransportFodGuaranteeType(bankGuaranteeRequestData.getTranslationLanguage().getLanguage(),
                                passengerTransport.getFodGuarantee())
                                : "N.A")
                        .license(passengerTransport.getLicenseNumber())
                        .bgReferenceNumber(passengerTransport.getBankGuaranteeId())
                        .contractAmount(guaranteeDetails.getBgAmount().doubleValue())
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .build())
                .bankGuaranteeDescription(passengerTransport.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForAbstractPromise(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForAbstractPromise] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PromiseAbstract promiseAbstract = (PromiseAbstract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .promiseEndDate(promiseAbstract.getPromiseEndDate())
                        .contractDescription(promiseAbstract.getContractDescription())
                        .contractReference(promiseAbstract.getReferenceNumber())
                        .build())
                .bankGuaranteeDescription(promiseAbstract.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForPublicContractPromise(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForPublicContractPromise] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        PromisePublicContract promisePublicContract = (PromisePublicContract) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .promiseEndDate(promisePublicContract.getPromiseEndDate())
                        .contractDescription(promisePublicContract.getTitle())
                        .contractReference(promisePublicContract.getContractDescription())
                        .build())
                .bankGuaranteeDescription(promisePublicContract.getBankGuaranteeEnd())
                .build();
    }

    private GuaranteeInformationDto prepareGuaranteeDetailForCustomizedText(BankGuaranteeRequestData bankGuaranteeRequestData) {
        log.info("ConnectDotMapper [prepareGuaranteeDetailForMoneyRetentionBond] call");
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        CustomizedText customizedText = (CustomizedText) JsonUtils.convert(guaranteeDetails.getBankGuarantee(), guaranteeDetails.getBgCode().getClassType());
        return GuaranteeInformationDto.builder()
                .contractDetailPayload(ContractDetailPayload.builder()
                        .contractCurrency(currencyService.getCurrencyCodeByValue(guaranteeDetails.getBgCurrency()))
                        .contractReference(customizedText.getReferenceNumber())
                        .contractDescription(customizedText.getContractDescription())
                        .comments(customizedText.getComments())
                        .expiryCode(customizedText.getBankGuaranteeEndType().name())
                        .bgMaturityDate(customizedText.getMaturityDate())
                        .build())
                .bankGuaranteeDescription(customizedText.getBankGuaranteeEnd())
                .build();

    }
}