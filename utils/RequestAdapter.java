package com.ing.bankguarantees.remote.utils;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.CallbackDto;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.FinancialInformationData.ContractDetailData;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.enums.PegaCaseType;
import com.ing.bankguarantees.models.enums.ServiceActivitiesCode;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories.Signatory;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingRequest;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.AgreementDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DocumentPlaceHolderIn;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DossierType;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.RequestDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateDossierDataInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementRequest;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.CSIHubInvolvedPartiesRequest;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.request.InvolvedPartyCsiRequest;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarRequest;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.docsign.CallbackEvent;
import com.ing.docsign.callback.DetailedCallbackEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestAdapter {

    private static final String DOSSIER_ACCEPTED_STATUS = "Accepted";

    public static AccountBalanceInput getAccountDetailRequest(ProductAgreementAccount account) {

        return AccountBalanceInput.builder()
                .accountName(account.getAccountName())
                .uuid(account.getUuid())
                .accountCurrency(account.getCurrency())
                .ibanNumber(account.getIban())
                .build();

    }

    public static CreditBalanceInput getCreditLineRequest(ProductAgreementAccount account) {

        return CreditBalanceInput.builder()
                .creditLineAccountNumber(Long.valueOf(account.getCreditLineAccountNumber()))
                .accountCurrency(account.getCurrency())
                .accountName(account.getAccountName())
                .uuid(account.getUuid())
                .build();
    }

    public static CreditBalanceInput getCreditLineRequest(ContractDetailData contractDetailData) {

        return CreditBalanceInput.builder()
                .creditLineAccountNumber(contractDetailData.getCreditLineAccountNumber().longValue())
                .accountCurrency(contractDetailData.getCurrency())
                .accountName(contractDetailData.getAccountName())
                .uuid(contractDetailData.getUuid())
                .build();
    }


    public static CSIHubInvolvedPartiesRequest getCsiCbeRequest(String cbeNo) {

        return CSIHubInvolvedPartiesRequest.builder()
                .involvedParty(InvolvedPartyCsiRequest.builder()
                        .involvedPartyRegistration(InvolvedPartyCsiRequest.InvolvedPartyRegistration.builder()
                                .registrationAuthority(2)
                                .registrationNumber(cbeNo)
                                .build())
                        .build())
                .build();
    }

    public static InvolvePartyRequest getInvolvePartyRequest(String uuid, Boolean individual) {

        return InvolvePartyRequest.builder()
                .individual(individual)
                .uuid(uuid)
                .build();
    }

    public static RequestDossierDataInput getRequestDossierIn(String orgId, String requestId) {

        return RequestDossierDataInput.builder()
                .legalEntityId(orgId)
                .requestId(requestId)
                .build();
    }

    public static AgreementDossierDataInput getRequestAgreementIn(String reqRespId, String requestId, String legalEntityId) {

        return AgreementDossierDataInput.builder()
                .reqResponseId(reqRespId)
                .requestId(requestId)
                .legalEntityId(legalEntityId)
                .build();
    }

    public static UpdateDossierDataInput getUpdateDossierDataInRequest(String agreementDossierId,
                                                                       DossierType dossierType) {
        return UpdateDossierDataInput.builder()
                .status(DOSSIER_ACCEPTED_STATUS)
                .dossierType(dossierType)
                .dossierId(agreementDossierId)
                .build();
    }

    public static DocumentPlaceHolderIn getRequestPlaceholderIn(String reqRespId, DocumentType documentType, Locale locale) {

        return DocumentPlaceHolderIn.builder()
                .locale(locale)
                .documentType(documentType)
                .dossierId(reqRespId)
                .build();
    }


    public static PegaCreateCaseInput getPegaInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documentList,
                                                   PegaCaseType pegaCaseType) {

        return PegaCreateCaseInput.builder()
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .documents(documentList)
                .pegaCaseType(pegaCaseType)
                .build();
    }

    public static IntakeApiInput getIntakeApiInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documentList) {

        return IntakeApiInput.builder()
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .documents(documentList)
                .build();
    }

    public static BookingRequest getBookingRequest(String masterRefNumber, BigDecimal accountNumber, RequestType requestType,
                                                   BigDecimal klcNumber, double bgAmount, String currency) {
        return BookingRequest.builder()
                .requestType(requestType)
                .accountNumber(accountNumber)
                .klcNumber(klcNumber)
                .reservationNumber(CommonUtils.convertMasterRefToKlcNumber(masterRefNumber))
                .currency(currency)
                .bgAmount(bgAmount)
                .build();
    }

    public static CreditLineArrangementRequest getCreditLineArrangementRequest(String masterRefNumber, RequestType requestType,
                                                                               String accountNumber, double bgAmount, String currency) {
        return CreditLineArrangementRequest.builder()
                .requestType(requestType)
                .reservationNumber(CommonUtils.convertMasterRefToKlcNumber(masterRefNumber))
                .accountNumber(accountNumber)
                .currency(currency)
                .bgAmount(bgAmount)
                .build();
    }

    public static AlerRetrieveSignatoriesInput getAlerRetrieveSignatoriesRequestInput(AccessToken accessToken, String legalEntityId, Locale translation) {

        return AlerRetrieveSignatoriesInput.builder()
                .accessToken(accessToken)
                .organisationId(legalEntityId)
                .translation(translation)
                .build();
    }

    public static Signatory getSoleProprietorSignatory(String lrId) {
        return Signatory.builder()
                .signatoryUUID(lrId)
                .isActive(true)
                .signingPower(1)
                .build();
    }
    
    public static PermissionRequest getPermissionRequest(AccessToken accessToken, ServiceActivitiesCode serviceActivitiesCode, String paramValue) {

        return PermissionRequest.builder()
                .accessToken(accessToken)
                .serviceActivitiesCode(serviceActivitiesCode)
                .paramValue(paramValue)
                .build();

    }

    public static HolidayCalendarRequest getHolidayCalenderRequest(LocalDate from, LocalDate to) {
        return HolidayCalendarRequest.builder()
                .fromDate(from)
                .toDate(to)
                .build();
    }

    public static CallbackDto getIntermediateCallbackDto(DetailedCallbackEvent intermediateCallbackEvent) {
        return CallbackDto.builder()
                .intermediate(true)
                .agreementId(intermediateCallbackEvent.getDarInfo().getAgreementId())
                .darUuid(intermediateCallbackEvent.getDarInfo().getDarUuid())
                .operations(intermediateCallbackEvent.getOperations())
                .status(intermediateCallbackEvent.getDarInfo().getStatus())
                .actorUuid(intermediateCallbackEvent.getOperationActorUuid())
                .build();

    }

    public static CallbackDto getCallbackDto(CallbackEvent callbackEvent) {
        return CallbackDto.builder()
                .intermediate(false)
                .agreementId(callbackEvent.getAgreementId())
                .darUuid(callbackEvent.getDarUuid())
                .status(callbackEvent.getStatus())
                .build();

    }

}
