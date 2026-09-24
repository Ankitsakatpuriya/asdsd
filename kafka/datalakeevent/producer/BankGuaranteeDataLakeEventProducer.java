package com.ing.bankguarantees.remote.kafka.datalakeevent.producer;

import com.ing.bankguarantees.avro.*;
import com.ing.bankguarantees.avro.guaranteetype.*;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.remote.kafka.datalakeevent.model.BgRequestDataLakeEventDto;
import com.ing.bankguarantees.remote.kafka.datalakeevent.model.DataLakeEventDto;
import com.ing.bankguarantees.remote.kafka.datalakeevent.utils.DataLakeUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BankGuaranteeDataLakeEventProducer extends DataLakeEventProducer {

    private static final String IND_ADD_DELIMITER = "IND-ADD";
    private static final String ORG_ADD_DELIMITER = "ORG-ADD";
    private static final String IND_IDENT_DELIMITER = "IND-IDENT";
    private static final String ORG_IDENT_DELIMITER = "ORG-IDENT";
    private static final String LR_IDENT_DELIMITER = "LR-IDENT";
    private static final String LR_DELIMITER = "LR";
    private static final String STP_RESULT_DELIMITER = "STPRESULT";
    private static final String NA = "N.A";

    public BankGuaranteeDataLakeEventProducer(DataLakeEventGateway dataLakeEventGateway) {
        super(dataLakeEventGateway);
    }

    @Override
    public BankGuaranteeEventBody getEventSpecificData(DataLakeEventDto dataLakeEventDto) {
        BgRequestDataLakeEventDto bgRequestDataLakeEventDto = (BgRequestDataLakeEventDto) dataLakeEventDto;
        log.info("start preparing data lake event message for request id {}", bgRequestDataLakeEventDto.getBankGuaranteeRequest().getRequestId());
        return prepareBankGuaranteeEventBody(bgRequestDataLakeEventDto.getBankGuaranteeRequest());
    }

    private BankGuaranteeEventBody prepareBankGuaranteeEventBody(BankGuaranteeRequest bankGuaranteeRequest) {
        BankGuaranteeEventBody bankGuaranteeEventBody = new BankGuaranteeEventBody();
        BankGuaranteeRequestData bankGuaranteeRequestData = prepareBankGuaranteeRequestDataAvro(bankGuaranteeRequest);
        StpResultDataSet stpResultDataSet = prepareStpResults(bankGuaranteeRequest);
        bankGuaranteeEventBody.setId(bankGuaranteeRequest.getRequestId());
        bankGuaranteeEventBody.setStatus(BankGuaranteeRequestStatus.valueOf(bankGuaranteeRequest.getStatus().name()));
        bankGuaranteeEventBody.setSessionId(bankGuaranteeRequest.getSessionId());
        bankGuaranteeEventBody.setCreatedBy(bankGuaranteeRequest.getCreatedBy());
        bankGuaranteeEventBody.setUpdatedBy(bankGuaranteeRequest.getUpdatedBy());
        bankGuaranteeEventBody.setDarId(bankGuaranteeRequest.getBgRequest().getDarId());
        bankGuaranteeEventBody.setMasterReferenceNumber(bankGuaranteeRequest.getMasterReferenceNumber());
        bankGuaranteeEventBody.setPegaCaseId(bankGuaranteeRequest.getPegaCaseId());
        bankGuaranteeEventBody.setCreatedTimestamp(bankGuaranteeRequest.getCreatedAt().toString());
        bankGuaranteeEventBody.setUpdatedTimestamp(bankGuaranteeRequest.getUpdatedAt().toString());
        bankGuaranteeEventBody.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeEventBody.setStpResult(stpResultDataSet);
        bankGuaranteeEventBody.setTotalRowCountInfo(prepareTotalCountInfo(bankGuaranteeRequestData, stpResultDataSet));
        return bankGuaranteeEventBody;
    }


    private BankGuaranteeRequestData prepareBankGuaranteeRequestDataAvro(BankGuaranteeRequest bgRequest) {

        BankGuaranteeRequestData bgRequestDataAvro = new BankGuaranteeRequestData();
        bgRequestDataAvro.setIssueToAnotherParty(bgRequest.getBgRequest().isIssueToAnotherParty());
        bgRequestDataAvro.setTranslationLanguage(bgRequest.getBgRequest().getTranslationLanguage().toString());
        bgRequestDataAvro.setReferenceNumber(Optional.ofNullable(bgRequest.getBgRequest().getReferenceNumber()).orElse(NA));
        bgRequestDataAvro.setSigned(bgRequest.getBgRequest().isSigned());
        bgRequestDataAvro.setSigningAllowed(bgRequest.getBgRequest().isSigningAllowed());
        bgRequestDataAvro.setWbCustomer(bgRequest.getBgRequest().isWbCustomer());
        bgRequestDataAvro.setStp(bgRequest.getBgRequest().isStp());
        bgRequestDataAvro.setInstructingParty(getInstructingPartyData(bgRequest));
        bgRequestDataAvro.setApplicant(getApplicantData(bgRequest));
        bgRequestDataAvro.setBeneficiary(getBeneficiaryData(bgRequest).orElse(null));
        bgRequestDataAvro.setFinancialInformation(getFinancialInformationData(bgRequest));
        bgRequestDataAvro.setDeliveryInformation(getDeliveryInformationData(bgRequest));
        bgRequestDataAvro.setLegalRepresentatives(getLegalRepresentativeData(bgRequest));
        bgRequestDataAvro.setGuaranteeDetails(getGuaranteeDetailsDataAvro(bgRequest));
        bgRequestDataAvro.setAlerDetails(getAlerDetailsData(bgRequest));
        bgRequestDataAvro.setDossierInformation(getDossierInformation(bgRequest));
        return bgRequestDataAvro;
    }


    private static InstructingPartyData getInstructingPartyData(BankGuaranteeRequest bgRequest) {
        InstructingPartyData instructingParty = JsonUtils.convert(bgRequest.getBgRequest().getInstructingParty(), InstructingPartyData.class);
        updateDigitalAddressIds(instructingParty.getIndividual().getDigitalAddresses(), bgRequest.getRequestId(), IND_ADD_DELIMITER);
        updateDigitalAddressIds(instructingParty.getOrganisation().getDigitalAddresses(), bgRequest.getRequestId(), ORG_ADD_DELIMITER);
        updateInternalIdentifierIds(instructingParty.getIndividual().getInternalIdentifiers(), bgRequest.getRequestId(), IND_IDENT_DELIMITER);
        updateInternalIdentifierIds(instructingParty.getOrganisation().getInternalIdentifiers(), bgRequest.getRequestId(), ORG_IDENT_DELIMITER);
        return instructingParty;
    }

    private static ApplicantData getApplicantData(BankGuaranteeRequest bgRequest) {
        return JsonUtils.convert(bgRequest.getBgRequest().getApplicant(), ApplicantData.class);
    }

    private static List<LegalRepresentativeData> getLegalRepresentativeData(BankGuaranteeRequest bgRequest) {
        int count = 0;
        List<LegalRepresentativeData> lrDataList = JsonUtils.convertToList(bgRequest.getBgRequest().getLegalRepresentatives(),
                new TypeReference<>() {
                });
        for (LegalRepresentativeData lrData : lrDataList) {
            String lrId = DataLakeUtils.getDataLakeId(bgRequest.getRequestId(), LR_DELIMITER, ++count);
            lrData.setId(lrId);
            lrData.setPreferredLanguage(Optional.ofNullable(lrData.getPreferredLanguage()).orElse(NA));
            lrData.setSigningPower(Optional.ofNullable(lrData.getSigningPower()).orElse(2));
            updateInternalIdentifierIds(lrData.getInternalIdentifiers(), lrId, LR_IDENT_DELIMITER);
        }
        return lrDataList;
    }

    private GuaranteeDetailsData getGuaranteeDetailsDataAvro(BankGuaranteeRequest bankGuaranteeRequest) {
        GuaranteeDetailsData guaranteeDetailsData = new GuaranteeDetailsData();
        guaranteeDetailsData.setBgCode(BankGuaranteeCode.valueOf(bankGuaranteeRequest.getBgRequest().getGuaranteeDetails().getBgCode().name()));
        guaranteeDetailsData.setBgLanguage(bankGuaranteeRequest.getBgRequest().getGuaranteeDetails().getBgLanguage().name());
        guaranteeDetailsData.setBgAmount(bankGuaranteeRequest.getBgRequest().getGuaranteeDetails().getBgAmount());
        guaranteeDetailsData.setBgCurrency(bankGuaranteeRequest.getBgRequest().getGuaranteeDetails().getBgCurrency());
        guaranteeDetailsData.setBankGuarantee(DataLakeUtils.getGuaranteeDetailsDataAvro(bankGuaranteeRequest.getBgRequest()));
        setDefaultGuaranteeValues(guaranteeDetailsData);
        return guaranteeDetailsData;
    }

    private void setDefaultGuaranteeValues(GuaranteeDetailsData guaranteeDetailsData) {

        switch (guaranteeDetailsData.getBgCode()) {
            case CUSTOMIZED_TEXT: {
                CustomizedText bankGuarantee = (CustomizedText) guaranteeDetailsData.getBankGuarantee();
                if (StringUtils.isEmpty(bankGuarantee.getSubBgCode()))
                    bankGuarantee.setSubBgCode("Other");
            }
            break;
            case WOODS_BG_DISCHARGE: {
                WoodsDischarge bankGuarantee = (WoodsDischarge) guaranteeDetailsData.getBankGuarantee();
                if (ObjectUtils.isEmpty(bankGuarantee.getReplacePromise())) {
                    bankGuarantee.setReplacePromise(false);
                }
            }
            break;
            case WOODS_BG_PRIVATE: {
                WoodsPrivate bankGuarantee = (WoodsPrivate) guaranteeDetailsData.getBankGuarantee();
                if (ObjectUtils.isEmpty(bankGuarantee.getReplacePromise())) {
                    bankGuarantee.setReplacePromise(false);
                }
            }
            break;
            case WOODS_BGVLA_PUBLIC: {
                WoodsVLAPublic bankGuarantee = (WoodsVLAPublic) guaranteeDetailsData.getBankGuarantee();
                if (ObjectUtils.isEmpty(bankGuarantee.getReplacePromise())) {
                    bankGuarantee.setReplacePromise(false);
                }
            }
            break;
            case WOODS_BGWAL_PUBLIC: {
                WoodsWALPublic bankGuarantee = (WoodsWALPublic) guaranteeDetailsData.getBankGuarantee();
                if (ObjectUtils.isEmpty(bankGuarantee.getReplacePromise())) {
                    bankGuarantee.setReplacePromise(false);
                }
            }
            break;

        }

    }

    private static AlerDetailsData getAlerDetailsData(BankGuaranteeRequest bgRequest) {
        com.ing.bankguarantees.models.domain.AlerDetailsData alerDetails = bgRequest.getBgRequest().getAlerDetails();
        AlerDetailsData alerDetailsData = new AlerDetailsData();
        if (ObjectUtils.isNotEmpty(alerDetails)) {
            alerDetailsData.setInvokeAler(alerDetails.isInvokeAler());
            alerDetailsData.setCode(ObjectUtils.isNotEmpty(alerDetails.getAlerErrorDetailData()) ? alerDetails.getAlerErrorDetailData().getCode() : null);
            alerDetailsData.setMessage(ObjectUtils.isNotEmpty(alerDetails.getAlerErrorDetailData()) ? alerDetails.getAlerErrorDetailData().getMessage() : null);
            alerDetailsData.setSigners(String.join(",", alerDetails.getSigners()));
            alerDetailsData.setTransactionId(alerDetails.getTransactionId());
            alerDetailsData.setTransactionStatus(alerDetails.getTransactionStatus());
        }
        return alerDetailsData;
    }

    private TotalRowCountInfo prepareTotalCountInfo(BankGuaranteeRequestData bankGuaranteeRequestData, StpResultDataSet stpResultDataSet) {
        BankGuaranteeCode bgCode = bankGuaranteeRequestData.getGuaranteeDetails().getBgCode();
        ContractDetailData creditLine = bankGuaranteeRequestData.getFinancialInformation().getCreditLine();
        DossierData dossierInformation = bankGuaranteeRequestData.getDossierInformation();
        AlerDetailsData alerDetails = bankGuaranteeRequestData.getAlerDetails();
        List<LegalRepresentativeData> legalRepresentatives = bankGuaranteeRequestData.getLegalRepresentatives();
        TotalRowCountInfo totalRowCountInfo = new TotalRowCountInfo();
        totalRowCountInfo.setTotalRowsInBankGuaranteeRequest(1);
        totalRowCountInfo.setTotalRowsInIndividual(1);
        totalRowCountInfo.setTotalRowsInOrganisation(1);
        totalRowCountInfo.setTotalRowsInBeneficiary(ObjectUtils.isNotEmpty(bankGuaranteeRequestData.getBeneficiary()) ? 1 : 0);
        totalRowCountInfo.setTotalRowsInApplicant(ObjectUtils.isNotEmpty(bankGuaranteeRequestData.getApplicant()) ? 1 : 0);
        totalRowCountInfo.setTotalRowsInAccountToDebited(1);
        totalRowCountInfo.setTotalRowsInCreditLine(ObjectUtils.isNotEmpty(creditLine) ? 1 : 0);
        totalRowCountInfo.setTotalRowsInGuaranteeDetails(1);
        totalRowCountInfo.setTotalRowsInInternalIdentifier((int) getInternalIdentifierRowCount(bankGuaranteeRequestData));
        totalRowCountInfo.setTotalRowsInDigitalAddress(getDigitalAddressRowCount(bankGuaranteeRequestData));
        totalRowCountInfo.setTotalRowsInStpResults(stpResultDataSet.getStpResults().size());
        totalRowCountInfo.setTotalRowsInAlerDetail(ObjectUtils.isNotEmpty(alerDetails) ? 1 : 0);
        totalRowCountInfo.setTotalRowsInDossierInformation(ObjectUtils.isNotEmpty(dossierInformation) ? 1 : 0);
        totalRowCountInfo.setTotalRowsInDeliveryInformation(1);
        totalRowCountInfo.setTotalRowsInLegalRepresentatives(CollectionUtils.size(legalRepresentatives));
        totalRowCountInfo.setTotalRowsInAbstractProm(bgCode == BankGuaranteeCode.ABSTRACT_PROM ? 1 : 0);
        totalRowCountInfo.setTotalRowsInAdvancePayment(bgCode == BankGuaranteeCode.ADVANCE_PAYMENT ? 1 : 0);
        totalRowCountInfo.setTotalRowsInBidBond(bgCode == BankGuaranteeCode.BID_BOND ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsPromVla(bgCode == BankGuaranteeCode.WOODS_PROM_VLA ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsPromB(bgCode == BankGuaranteeCode.WOODS_PROM_B ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsPromA(bgCode == BankGuaranteeCode.WOODS_PROM_A ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsBgWalPublic(bgCode == BankGuaranteeCode.WOODS_BGWAL_PUBLIC ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsBgVlaPublic(bgCode == BankGuaranteeCode.WOODS_BGVLA_PUBLIC ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsBgPrivate(bgCode == BankGuaranteeCode.WOODS_BG_PRIVATE ? 1 : 0);
        totalRowCountInfo.setTotalRowsInWoodsBgDischarge(bgCode == BankGuaranteeCode.WOODS_BG_DISCHARGE ? 1 : 0);
        totalRowCountInfo.setTotalRowsInStateLottery(bgCode == BankGuaranteeCode.STATE_LOTTERY ? 1 : 0);
        totalRowCountInfo.setTotalRowsInRental(bgCode == BankGuaranteeCode.RENTAL ? 1 : 0);
        totalRowCountInfo.setTotalRowsInRealEstate(bgCode == BankGuaranteeCode.REAL_ESTATE ? 1 : 0);
        totalRowCountInfo.setTotalRowsInPublicContractProm(bgCode == BankGuaranteeCode.PUBLIC_CONTRACT_PROM ? 1 : 0);
        totalRowCountInfo.setTotalRowsInPerformanceBond(bgCode == BankGuaranteeCode.PERFORMANCE_BOND ? 1 : 0);
        totalRowCountInfo.setTotalRowsInPayment(bgCode == BankGuaranteeCode.PAYMENT_GUARANTEE ? 1 : 0);
        totalRowCountInfo.setTotalRowsInPassengerTransport(bgCode == BankGuaranteeCode.PASSENGER_TRANSPORT ? 1 : 0);
        totalRowCountInfo.setTotalRowsInOvam(bgCode == BankGuaranteeCode.OVAM ? 1 : 0);
        totalRowCountInfo.setTotalRowsInOperatorsTransport(bgCode == BankGuaranteeCode.OPERATORS_TRANSPORT ? 1 : 0);
        totalRowCountInfo.setTotalRowsInMoneyRetentionBond(bgCode == BankGuaranteeCode.MONEY_RETENTION_BOND ? 1 : 0);
        totalRowCountInfo.setTotalRowsInGoodsTransport(bgCode == BankGuaranteeCode.GOODS_TRANSPORT ? 1 : 0);
        totalRowCountInfo.setTotalRowsInDck(bgCode == BankGuaranteeCode.DCK_CDC ? 1 : 0);
        totalRowCountInfo.setTotalRowsInCustomTypeTwo(bgCode == BankGuaranteeCode.CUSTOM_2 ? 1 : 0);
        totalRowCountInfo.setTotalRowsInCustomTypeOne(bgCode == BankGuaranteeCode.CUSTOM_1 ? 1 : 0);
        totalRowCountInfo.setTotalRowsInCustomTypeFive(bgCode == BankGuaranteeCode.CUSTOM_5 ? 1 : 0);
        totalRowCountInfo.setTotalRowsInCustomTypeFour(bgCode == BankGuaranteeCode.CUSTOM_4 ? 1 : 0);
        totalRowCountInfo.setTotalRowsInCustomizedText(bgCode == BankGuaranteeCode.CUSTOMIZED_TEXT ? 1 : 0);
        totalRowCountInfo.setTotalRowsInPublicContract(bgCode == BankGuaranteeCode.PUBLIC_CONTRACT ? 1 : 0);
        return totalRowCountInfo;
    }


    private int getDigitalAddressRowCount(BankGuaranteeRequestData bankGuaranteeRequestData) {

        int individualCount = bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses().size();
        int orgCount = bankGuaranteeRequestData.getInstructingParty().getOrganisation().getDigitalAddresses().size();
        return individualCount + orgCount;
    }

    private long getInternalIdentifierRowCount(BankGuaranteeRequestData bankGuaranteeRequestData) {

        int individualCount = bankGuaranteeRequestData.getInstructingParty().getIndividual().getInternalIdentifiers().size();
        int orgCount = bankGuaranteeRequestData.getInstructingParty().getOrganisation().getInternalIdentifiers().size();
        long lrCount = bankGuaranteeRequestData.getLegalRepresentatives().stream()
                .mapToLong(legalRepresentative -> legalRepresentative.getInternalIdentifiers().size())
                .sum();
        return (individualCount + orgCount + lrCount);
    }

    private static StpResultDataSet prepareStpResults(BankGuaranteeRequest bankGuaranteeRequest) {
        StpResultDataSet stpResultSet = JsonUtils.convert(bankGuaranteeRequest.getBgRequest().getStpResultDataSet(), StpResultDataSet.class);
        int count = 0;
        for (STPResultData stpResultData : stpResultSet.getStpResults()) {
            stpResultData.setId(DataLakeUtils.getDataLakeId(bankGuaranteeRequest.getRequestId(), STP_RESULT_DELIMITER, ++count));
        }
        return stpResultSet;
    }


    private DossierData getDossierInformation(BankGuaranteeRequest bgRequest) {
        return JsonUtils.convert(bgRequest.getBgRequest().getDossierInformation(), DossierData.class);
    }


    private static void updateDigitalAddressIds(List<DigitalAddressData> digitalAddresses, String requestId, String delimiter) {
        int count = 0;
        for (DigitalAddressData digitalAddress : digitalAddresses) {
            digitalAddress.setId(DataLakeUtils.getDataLakeId(requestId, delimiter, ++count));
            digitalAddress.setFullDigitalAddress(Optional.ofNullable(digitalAddress.getFullDigitalAddress()).orElse(NA));
        }
    }

    private static void updateInternalIdentifierIds(List<Identifier> identifierList, String requestId, String delimiter) {
        int count = 0;
        for (Identifier identifier : identifierList) {
            identifier.setId(DataLakeUtils.getDataLakeId(requestId, delimiter, ++count));
        }
    }

    private static FinancialInformationData getFinancialInformationData(BankGuaranteeRequest bgRequest) {
        return JsonUtils.convert(bgRequest.getBgRequest().getFinancialInformation(), FinancialInformationData.class);
    }

    private static DeliveryInformationData getDeliveryInformationData(BankGuaranteeRequest bgRequest) {
        return JsonUtils.convert(bgRequest.getBgRequest().getDeliveryInformation(), DeliveryInformationData.class);
    }

    private static Optional<BeneficiaryData> getBeneficiaryData(BankGuaranteeRequest bgRequest) {
        return Optional.ofNullable(JsonUtils.convert(bgRequest.getBgRequest().getBeneficiary(), BeneficiaryData.class));
    }


}
