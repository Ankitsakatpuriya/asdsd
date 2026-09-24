package com.ing.bankguarantees.util;

import com.ing.apisdk.toolkit.trust.accesstoken.*;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.BankGuaranteeDataSet;
import com.ing.bankguarantees.models.BankGuaranteeDataSet.BankGuaranteeData;
import com.ing.bankguarantees.models.DossierRequestData;
import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.cache.CountryDataCache;
import com.ing.bankguarantees.models.domain.*;
import com.ing.bankguarantees.models.domain.ApplicantData.ApplicantPostalAddressData;
import com.ing.bankguarantees.models.domain.BeneficiaryData.BeneficiaryOrganisationNameData;
import com.ing.bankguarantees.models.domain.BeneficiaryData.BeneficiaryPostalAddressData;
import com.ing.bankguarantees.models.enums.*;
import com.ing.bankguarantees.models.guaranteetype.*;
import com.ing.bankguarantees.models.request.*;
import com.ing.bankguarantees.models.response.*;
import com.ing.bankguarantees.models.response.CountryDatasetResponse.CountryData;
import com.ing.bankguarantees.models.response.InstructingPartyResponse.IndividualName;
import com.ing.bankguarantees.models.response.InstructingPartyResponse.IndividualResponse;
import com.ing.bankguarantees.models.response.InstructingPartyResponse.OrganisationResponse;
import com.ing.bankguarantees.models.response.LegalEntityResponse.LegalEntityOrganisationNameResponse;
import com.ing.bankguarantees.models.response.LegalEntityResponse.LegalEntityPostalAddressResponse;
import com.ing.bankguarantees.remote.kafka.datalakeevent.model.BgRequestDataLakeEventDto;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
import com.ing.bankguarantees.remote.rest.aler.model.request.AlerRetrieveSignatoriesInput;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse.ErrorResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse.SignatoryResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories.AlerErrorDetails;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories.Signatory;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingRequest;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingResponse;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.AgreementDossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.DossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.response.CreditLineArrangementOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ApplicantDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BeneficiaryDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput.PamRequestIdentifier;
import com.ing.bankguarantees.remote.rest.creditdecision.model.response.CreditDecisionResponse;
import com.ing.bankguarantees.remote.rest.creditdecision.model.response.CreditDecisionResponse.DecisionResult;
import com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.model.response.*;
import com.ing.bankguarantees.remote.rest.dar.DarProperties;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarInput;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse;
import com.ing.bankguarantees.remote.rest.emeafx.model.request.FxRatesConversionInput;
import com.ing.bankguarantees.remote.rest.emeafx.model.response.FxRateConversionResponse;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.response.GarOutput;
import com.ing.bankguarantees.remote.rest.gess.GessProperties;
import com.ing.bankguarantees.remote.rest.gess.GessProperties.GessCoordinateConfigProperties;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignInput;
import com.ing.bankguarantees.remote.rest.gess.model.request.SignPosition;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.BankHoliday;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiInput;
import com.ing.bankguarantees.remote.rest.intake.model.request.IntakeApiRequest;
import com.ing.bankguarantees.remote.rest.intake.model.response.IntakeApiResponse;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierResponse;
import com.ing.bankguarantees.remote.rest.involveparty.grantees.model.response.*;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request.InvolvePartyRequest;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.DigitalAddressOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IdentifierOnePamResponse;
import com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.response.InvolvedPartyOnePamResponse.IndividualOnePamResponse.IndividualNameOnePamResponse;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.KfcCreditOverviewProperties;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse.SignaleticInformationResponse;
import com.ing.bankguarantees.remote.rest.namescreening.model.request.NameScreeningInput;
import com.ing.bankguarantees.remote.rest.namescreening.model.response.NameScreeningResponse;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse.PartyResponse;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseRequest;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseResponse;
import com.ing.bankguarantees.remote.rest.permission.model.request.PermissionRequest;
import com.ing.bankguarantees.remote.rest.permission.model.response.PermissionResponse;
import com.ing.bankguarantees.remote.rest.permission.model.response.ServiceActivity;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeOutput;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse.ReferenceData;
import com.ing.bankguarantees.service.beneficiary.BeneficiaryProperties;
import com.ing.bankguarantees.service.finalization.FulfillmentProperties;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import com.ing.bankguarantees.utils.TracingHelper;
import com.ing.docsign.CallbackEvent;
import com.ing.docsign.callback.DarInfo;
import com.ing.docsign.callback.DetailedCallbackEvent;
import com.ing.tpa.esuite.notification.external.feedback.ExternalFeedbackEvent;
import com.ing.tpa.esuite.notificationapi.domain.EngagementSuiteNotificationEvent;
import io.opentelemetry.api.trace.SpanContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.ing.bankguarantees.remote.kafka.datalakeevent.utils.DataLakeUtils.getDataLakeEventName;
import static com.ing.bankguarantees.util.TestConstants.*;
import static com.ing.bankguarantees.utils.ConstantUtils.CSI;
import static com.ing.bankguarantees.utils.ConstantUtils.SPECIFIED_TEXT;
import static com.ing.bankguarantees.utils.JsonUtils.mapFileToObject;


@Slf4j
public class MockHelper {

    private static final String DOCUMENT_ID = "DOCUMENTID";
    private static final String DOSSIER_ID = "DOSSIER-001";
    private static final String ORG_TYPE = "ORG TYPE";
    private static final String ORG_FULL_NAME = "ORG FULL NAME";
    private static final String LEGAL_REP_EMAIL_2 = "LegalRepEmail2@example.com";
    private static final String LEGAL_REP_FULL_NAME_2 = "LegalRep2FullName";
    private static final String LEGAL_REP_EMAIL_1 = "LegalRepEmail1@example.com";
    private static final String LEGAL_REP_FULL_NAME_1 = "LegalRep1FullName";
    private static final String FULL_NAME = "FULLNAME";
    private static final String LEGAL_REP_ID = "LEGALREPID";
    private static final String LEGAL_ENTITY_FULLNAME = "FULLNAME";
    private static final String CIN_NUMBER = "CINNUMBER";
    private static final String ACCOUNT_UUID = "4efae44b-917b-4c08-8e2d-8598d995c162";
    private static final String ACCOUNT_NAME = "accountName";
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(150);
    private static final String IBAN_ACCOUNT_BE = "BE62510007547061";
    private static final String GUARANTEE_END_DATE = "Other";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CONTRACT_DESCRIPTION = "Project";
    private static final String HEADER_REQ_ID = "X-ING-REQ-ID";
    private static final String HEADER_SOURCE = "X-ING-SOURCE";
    private static final String URL_FORMAT = "https://api.ing.com/bankaccountnumbers/single/get";

    public static byte[] readFile(BankGuaranteeCode bankGuaranteeCode, String language) {
        try {
            String fileName = bankGuaranteeCode.name().concat("-").concat(language).concat(".pdf");
            return MockHelper.class.getClassLoader().getResourceAsStream("sample_bg_docs/".concat(fileName)).readAllBytes();
        } catch (NullPointerException | IOException exception) {
            log.error("MockHelper [readFile] File not found : {}", exception.getMessage());
            throw new BgosException(ErrorCode.NOT_FOUND, exception);
        }
    }

    public static ProductAgreementAccount getAcctBalanceProductAgreement() {
        return ProductAgreementAccount.builder()
                .iban("BE1267354572")
                .currency("EUR")
                .accountName("MockName")
                .uuid("mockUUID")
                .build();
    }

    public static AccountBalanceResponse getAcctBalanceResponse() {

        return AccountBalanceResponse.builder()
                .accounts(Collections.singletonList(AccountBalanceResponse.AccountResponse.builder()
                        .accountCurrency("003")
                        .ibanNumber("BE1267354572")
                        .balanceAmount(BigDecimal.valueOf(24749910.66))
                        .build()))
                .build();
    }

    public static ExternalIdentifierListResponse getExternalIdentifierResponse(String identifierType, String identifierValue) {

        return ExternalIdentifierListResponse.builder()
                .involvedPartyExternalIdentifiers(Collections.singletonList(ExternalIdentifierResponse.builder()
                        .involvedPartyExternalIdentifierType(identifierType)
                        .involvedPartyExternalIdentifierValue(identifierValue)
                        .build()))
                .build();
    }

    public static InvolvePartyRequest getInvolvePartyRequest() {

        return InvolvePartyRequest.builder()
                .individual(true)
                .uuid("uuid")
                .build();
    }

    public static ErrorItem CreateErrorItem(String fileName) {
        return mapFileToObject(fileName, ErrorItem.class);
    }

    public static List<Identifier> getIdentifier() {
        Identifier identifier = Identifier.builder()
                .type("CSI_BE")
                .value("99999999")
                .build();
        return List.of(identifier);
    }

    public static InvolvedPartyOnePamResponse.IndividualOnePamResponse getIndividualTelDigitalMobileOnePamResponse() {
        return InvolvedPartyOnePamResponse.IndividualOnePamResponse.builder()
                .individualName(IndividualNameOnePamResponse.builder()
                        .firstName1("Fname")
                        .lastName("Lname")
                        .build())
                .digitalAddresses(List.of(DigitalAddressOnePamResponse.builder()
                        .digitalAddressType("TEL_ADR")
                        .digitalAddressUsageType("MBL_TEL_NBR")
                        .fullDigitalAddress("FULL_DIGITAL")
                        .build()))
                .involvedPartyInternalIdentifiers(List.of(IdentifierOnePamResponse.builder()
                        .involvedPartyInternalIdentifierType(CSI)
                        .involvedPartyInternalIdentifierValue("99999999")
                        .build()))
                .build();
    }

    public static InvolvedPartyOnePamResponse.IndividualOnePamResponse getIndividualEmailDigitalAddressOnePamResponse() {
        return InvolvedPartyOnePamResponse.IndividualOnePamResponse.builder()
                .individualName(IndividualNameOnePamResponse.builder()
                        .firstName1("Fname")
                        .lastName("Lname")
                        .build())
                .digitalAddresses(List.of(DigitalAddressOnePamResponse.builder()
                        .digitalAddressType("EMAIL_ADR")
                        .digitalAddressUsageType("BSN_EMAIL")
                        .fullDigitalAddress("FULL_DIGITAL")
                        .build()))
                .involvedPartyInternalIdentifiers(List.of(IdentifierOnePamResponse.builder()
                        .involvedPartyInternalIdentifierType(CSI)
                        .involvedPartyInternalIdentifierValue("99999999")
                        .build()))
                .build();
    }

    public static GranteeGrantorResponse getGranteeGrantorResponse() {

        return GranteeGrantorResponse.builder()
                .involvedPartyInvolvedPartyRelationships(InvolvedPartyRelationship.builder()
                        .associatedPartyRelationships(List.of(AssociatedPartyResponse.builder()
                                .grantors(List.of(InvPartyGrantorResponse.builder()
                                        .id("12345")
                                        .type("Type")
                                        .involvedPartyType("PartyType")
                                        .build()))
                                .grantee(InvPartyGranteeResponse.builder()
                                        .id("2949e88c-943e-43fd-82d0-3342652a79be")
                                        .type("LGL_REP")
                                        .involvedPartyType("InvPartyType")
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static InvolvedPartyOnePamResponse getInvolvedPartyOnePamResponse() {


        return InvolvedPartyOnePamResponse.builder()
                .individual(InvolvedPartyOnePamResponse.IndividualOnePamResponse.builder()
                        .involvedPartyInternalIdentifiers(List.of(IdentifierOnePamResponse.builder()
                                .involvedPartyInternalIdentifierType(CSI)
                                .involvedPartyInternalIdentifierValue("99999999")
                                .build()))
                        .build())
                .organisation(InvolvedPartyOnePamResponse.OrganisationOnePamResponse.builder()
                        .organisationNames(List.of(InvolvedPartyOnePamResponse.OrganisationOnePamResponse.OrganisationNameOnePamResponse.builder()
                                .organisationNameType("LGL_NM")
                                .organisationName("MockOrganisation")
                                .build()))
                        .postalAddresses(List.of(InvolvedPartyOnePamResponse.PostalAddressOnePamResponse.builder()
                                .cityName("Brussels")
                                .postalAddressUsageType("CRSPD_ADR")
                                .unstructuredAddressLine1("unstructuredAddressLine1")
                                .houseNumber("53")
                                .houseNumberAddition("12")
                                .countryCode("BE")
                                .streetName("Mock Street")
                                .postalCode("440003")
                                .build()))
                        .legalForm("legalForm")
                        .involvedPartyGroups(List.of(InvolvedPartyOnePamResponse.OrganisationOnePamResponse.GroupResponse.builder()
                                .involvedPartyGroupType("SVC_SEGM_BE")
                                .involvedPartyGroupCode("205")
                                .build()))
                        .involvedPartyInternalIdentifiers(List.of(IdentifierOnePamResponse.builder()
                                .involvedPartyInternalIdentifierType(CSI)
                                .involvedPartyInternalIdentifierValue("123456789")
                                .build()))
                        .build())
                .build();
    }

    public static PermissionResponse getPermissionResponse() {
        return PermissionResponse.builder()
                .serviceActivities(List.of(new ServiceActivity("involved-parties:bank-guarantees:create")))
                .identifier("12345")
                .type("ORG")
                .build();
    }

    public static LegalEntityResponse getLegalEntityResponse() {


        return LegalEntityResponse.builder()
                .cinNumber("cbeNos")
                .organisationName(LegalEntityOrganisationNameResponse.builder()
                        .fullName("orgName")
                        .build())
                .postalAddress(LegalEntityPostalAddressResponse.builder()
                        .firstAddress("firstAddress")
                        .countryCode("countryCode")
                        .houseNumber("houseNumber")
                        .cityName("cityName")
                        .streetName("streetName")
                        .postalCode("postalCode")
                        .build())
                .build();
    }

    public static InstructingPartyResponse getInstructingPartyResponse() {
        return InstructingPartyResponse.builder()
                .individual(IndividualResponse.builder()
                        .legalRepId("legalRepId")
                        .individualName(IndividualName.builder()
                                .firstName("FName")
                                .lastName("LName")
                                .build())
                        .build())
                .organisation(OrganisationResponse.builder()
                        .cinNumber("cbeNumber")
                        .legalEntityId("orgUuid")
                        .build())
                .build();
    }

    public static InvolvedPartiesCsiHubResponse getInvolvedPartiesCsiHubResponseByKBOEmptyPostalAddress() {
        return InvolvedPartiesCsiHubResponse.builder().involvedParty(InvolvedPartyCsiResponse
                .builder().postalAddressContactPointUsage(null)
                .organisation(OrganisationCsiResponse.builder()
                        .organisationName(List.of(OrganisationNameCsiResponse
                                .builder()
                                .language(1)
                                .fullName("CSI SPRL")
                                .nameUsage(1)
                                .build()))
                        .build())
                .build()).build();
    }

    public static InvolvedPartiesCsiHubResponse getInvolvedPartiesCsiHubResponseByKBOBEmptyOrganisation() {

        return InvolvedPartiesCsiHubResponse.builder()
                .involvedParty(InvolvedPartyCsiResponse.builder()
                        .postalAddressContactPointUsage(List.of(PostalAddressContactPointUsageCsiResponse.builder()
                                .postalAddress(PostalAddressCsiResponse.builder()
                                        .postalCode(1000)
                                        .city("brussels")
                                        .firstAddressLine("Gare du midi")
                                        .countryCode("be")
                                        .build()).build()))
                        .organisation(OrganisationCsiResponse.builder()
                                .organisationName(null)
                                .build())
                        .build())
                .build();
    }

    public static InvolvedPartiesCsiHubResponse getInvolvedPartiesCsiHubResponse() {
        OrganisationNameCsiResponse organisationNameCsiResponse;
        EmailAddressContactPointUsageCsiResponse emailAddressContactPointUsageCsiResponse = EmailAddressContactPointUsageCsiResponse.builder()
                .emailAddress(EmailAddressCsiResponse.builder()
                        .emailAddressInformation("Email Address Information")
                        .build())
                .build();
        ArrayList<EmailAddressContactPointUsageCsiResponse> emailAdrList = new ArrayList<>();
        emailAdrList.add(emailAddressContactPointUsageCsiResponse);
        ArrayList<PostalAddressContactPointUsageCsiResponse> postalAddressContactPointUsageCsiRes = new ArrayList<PostalAddressContactPointUsageCsiResponse>();
        postalAddressContactPointUsageCsiRes.add(PostalAddressContactPointUsageCsiResponse.builder()
                .postalAddress(PostalAddressCsiResponse.builder()
                        .postalCode(1000)
                        .city("brussels")
                        .firstAddressLine("Gare du midi")
                        .countryCode("be")
                        .build()).build());
        organisationNameCsiResponse = OrganisationNameCsiResponse
                .builder().language(1).fullName("CSI SPRL").nameUsage(1).build();
        ArrayList<OrganisationNameCsiResponse> orgCsiList = new ArrayList<>();
        orgCsiList.add(organisationNameCsiResponse);
        return InvolvedPartiesCsiHubResponse.builder().involvedParty(InvolvedPartyCsiResponse
                .builder().postalAddressContactPointUsage(postalAddressContactPointUsageCsiRes)
                .emailAddressContactPointUsage(emailAdrList)
                .organisation(OrganisationCsiResponse.builder().organisationName(orgCsiList).build())
                .build()).build();
    }


    public static ReferenceDataMultilingualResponse getReferenceDataResponse() {

        return ReferenceDataMultilingualResponse.builder()
                .data(List.of(ReferenceData.builder()
                                .businessKey("BE")
                                .language("EN")
                                .translation("Belgium")
                                .build(),
                        ReferenceData.builder()
                                .businessKey("PT")
                                .language("EN")
                                .translation("Portugal")
                                .build()
                ))
                .build();
    }


    public static PermissionRequest getPermissionRequest(ServiceActivitiesCode serviceActivitiesCode, String paramValue) {

        return PermissionRequest.builder()
                .paramValue(paramValue)
                .serviceActivitiesCode(serviceActivitiesCode)
                .accessToken(getAccessToken())
                .build();
    }


    public static CountryDatasetResponse getCountryList() {
        return CountryDatasetResponse.builder()
                .countryData(List.of(CountryData.builder()
                        .businessKey("BE")
                        .translation("001")
                        .language("en")
                        .build()))
                .build();
    }

    public static CurrencyDatasetResponse getCurrencyResponse() {
        return CurrencyDatasetResponse.builder()
                .currencies(List.of(CurrencyDatasetResponse.CurrencyDataResponse.builder()
                        .code("AFN")
                        .value("961")
                        .build()))
                .build();
    }

    public static List<AccountBalanceOutput> getAcctBalanceOutput(AccountBalanceResponse response, AccountBalanceInput accountBalanceInput) {
        return response.getAccounts()
                .stream()
                .map(account -> AccountBalanceOutput.builder()
                        .balanceAmount(account.getBalanceAmount())
                        .accountCurrency(account.getAccountCurrency())
                        .ibanNumber(account.getIbanNumber())
                        .accountName(accountBalanceInput.getAccountName())
                        .uuid(accountBalanceInput.getUuid())
                        .build())
                .toList();
    }


    public static BankGuaranteeCodeSetResponse getBankGuaranteeCodeSetResponse() {
        return BankGuaranteeCodeSetResponse.builder()
                .codes(Stream.of(BankGuaranteeCode.values())
                        .map(bankGuaranteeCode -> BankGuaranteeCodeSetResponse.BankGuaranteeCodeResponse.builder()
                                .code(bankGuaranteeCode.name())
                                .build())
                        .toList())
                .build();
    }

    public static AccessToken getAccessToken() {
        Means means = new MeansBuilder()
                .setType("BE_MEANS_CAP_UCR_ID")
                .setId("8504bb6f-b648-43f8-bd19-a6a08df896b5")
                .build();

        Executor executor = new ExecutorBuilder()
                .setMeans(Collections.singletonList(means))
                .setGrant("grantId")
                .setLevelOfAssurance(2)
                .setPerson("2949e88c-943e-43fd-82d0-3342652a79be")
                .setProfile("2949e88c-943e-43fd-82d0-3342652a79be")
                .setType("customer")
                .build();

        AccessTokenClaimsSet claimsSet = new AccessTokenClaimsSetBuilder()
                .setIssuer("ING")
                .setCustomerServiceOrganisation("cso_be")
                .setIssuedAtTimestamp(0L)
                .setNotBeforeTimestamp(System.currentTimeMillis() / 1000)
                .setTimeToLive(16070400L)
                .setClientId("string")
                .setTraceEnabled(true)
                .setExecutor(executor)
                .setRequester(null)
                .setPinningType("string")
                .setPinningValue("string")
                .setScopes("personal_data")
                .setSetId("string")
                .build();

        return new AccessTokenBuilder().createAccessToken(claimsSet);
    }

    public static AccessToken getAccessToken(String requesterId, String sessionId, String profileId) {
        Means means = new MeansBuilder()
                .setType("BE_MEANS_CAP_UCR_ID")
                .setId("8504bb6f-b648-43f8-bd19-a6a08df896b5")
                .build();

        Executor executor = new ExecutorBuilder()
                .setMeans(Collections.singletonList(means))
                .setGrant("grantId").setLevelOfAssurance(2)
                .setPerson(requesterId)
                .setProfile(profileId)
                .setType("customer")
                .build();

        AccessTokenClaimsSet claimsSet = new AccessTokenClaimsSetBuilder()
                .setIssuer("ING").setCustomerServiceOrganisation("cso_be")
                .setIssuedAtTimestamp(0L).setNotBeforeTimestamp(System.currentTimeMillis() / 1000)
                .setTimeToLive(16070400L)
                .setClientId("string").setTraceEnabled(true)
                .setExecutor(executor)
                .setRequester(null)
                .setPinningType("string")
                .setPinningValue("string")
                .setScopes("personal_data")
                .setSetId(sessionId)
                .build();

        return new AccessTokenBuilder().createAccessToken(claimsSet);
    }

    public static CreditLineBalanceResponse getCreditLineResponse() {

        return CreditLineBalanceResponse.builder()
                .contractDetails(Collections.singletonList(CreditLineBalanceResponse.CreditDetailsResponse.builder()
                        .endDate("20250508")
                        .contractNumber(BigDecimal.valueOf(17109483L))
                        .availableAmount(CreditLineBalanceResponse.AvailableAmountResponse.builder()
                                .amount(BigDecimal.valueOf(799980000L))
                                .decimal(4)
                                .currency("003")
                                .build())
                        .build()))
                .build();

    }

    public static ProductAgreementAccount getCreditLineProductAgreement() {
        return ProductAgreementAccount.builder()
                .creditLineAccountNumber("384031884085")
                .currency("EUR")
                .accountName("Mock Account")
                .uuid("mockuuid")
                .build();
    }

    public static List<CreditBalanceOutput> getCreditBalanceOutput(CreditLineBalanceResponse creditLineBalanceResponse, CreditBalanceInput input) {

        return creditLineBalanceResponse.getContractDetails()
                .stream()
                .map(creditAccount -> CreditBalanceOutput.builder()
                        .klcNumber(creditAccount.getContractNumber())
                        .accountName(input.getAccountName())
                        .uuid(input.getUuid())
                        .endDate(LocalDate.of(2025, 5, 8))
                        .creditLineAccountNumber(BigDecimal.valueOf(input.getCreditLineAccountNumber()))
                        .availableAmount(CreditBalanceOutput.AvailableAmountOutput.builder()
                                .amount(creditAccount.getAvailableAmount().getAmount())
                                .currency(creditAccount.getAvailableAmount().getCurrency())
                                .decimal(creditAccount.getAvailableAmount().getDecimal())
                                .build())
                        .build())
                .toList();
    }

    public static List<CreditBalanceOutput> getCreditBalanceOutput(FinancialInformationData.ContractDetailData contractDetailData) {

        return List.of(CreditBalanceOutput.builder()
                .klcNumber(contractDetailData.getKlcNumber())
                .accountName(contractDetailData.getAccountName())
                .uuid(contractDetailData.getUuid())
                .creditLineAccountNumber(contractDetailData.getCreditLineAccountNumber())
                .availableAmount(CreditBalanceOutput.AvailableAmountOutput.builder()
                        .amount(contractDetailData.getAmount().multiply(BigDecimal.valueOf(100)))
                        .currency(contractDetailData.getCurrency())
                        .decimal(2)
                        .build())
                .build());

    }

    public static List<ProductAgreementAccount> getProductAgreementAccount() {
        return List.of(ProductAgreementAccount.builder()
                .uuid("2456234")
                .iban("IBAN")
                .accountName("alias")
                .creditLineAccountNumber("12345678910")
                .productType("BE_ING_PROF_CRN_AC")
                .currency("EUR")
                .build());
    }

    public static ProductAgreementResponse createProductAgreements(String fileName) {
        return mapFileToObject(fileName, ProductAgreementResponse.class);
    }

    public static FinancialDetailResponse getFinancialDetailResponse() {

        FinancialDetailResponse.FinancialCreditLineResponse financialCreditLineResponse = FinancialDetailResponse.FinancialCreditLineResponse.builder()
                .contractDetails(List.of(FinancialDetailResponse.FinancialContractDetailResponse.builder()
                        .creditLineAccountNumber(new BigDecimal(1234L))
                        .klcNumber(new BigDecimal(12345678L))
                        .build()))
                .build();
        FinancialDetailResponse.FinancialCurrentAccountResponse currentAccountResponse = FinancialDetailResponse.FinancialCurrentAccountResponse.builder()
                .accounts(List.of(FinancialDetailResponse.FinancialAccountResponse.builder()
                        .accountCurrency("EUR")
                        .balanceAmount(new BigDecimal(12345))
                        .ibanNumber("BE12345678")
                        .build()))
                .build();
        return new FinancialDetailResponse(financialCreditLineResponse, currentAccountResponse);

    }

    public static DossierDataResponse getDossierDataResponse() {
        return DossierDataResponse.builder()
                .id("id")
                .build();
    }

    public static DossierRequestData getDossierInput(String requesterId, String legalEntityId, AccessToken accessToken) {
        return DossierRequestData.builder()
                .requestId(requesterId)
                .accessToken(accessToken)
                .legalEntityId(legalEntityId)
                .build();
    }

    public static AgreementDossierDataResponse getAgreementDossierResponse(String dossierRespId, String agreementResponseId) {
        return AgreementDossierDataResponse.builder()
                .dossierId(dossierRespId)
                .id(agreementResponseId)
                .build();
    }

    public static Reporting getReporting(String requesterId) {
        return Reporting.builder()
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .requestId(UUID.randomUUID().toString())
                .status(BankGuaranteeRequestStatus.DRAFT)
                .build();
    }

    public static ApplicantDocumentPayload getApplicantDocumentPayload() {
        return ApplicantDocumentPayload.builder()
                .companyName("mock company name")
                .kboNumber("1234.567.890")
                .street("street")
                .city("city")
                .zip("440003")
                .country("Belgium")
                .build();
    }


    public static NameScreeningInput getNameScreeningInput() {
        return NameScreeningInput.builder()
                .organizationName("mock org name")
                .street("mock street")
                .houseNumber("mock number")
                .city("mock city")
                .zipCode("mock zip code")
                .countryCode("mock country code")
                .build();

    }

    public static NameScreeningResponse getNameScreeningResponse(Integer hitNumber) {

        List<NameScreeningResponse.HitsList> hitsLists = new ArrayList<>();
        for (int i = 0; i < hitNumber; i++) {
            hitsLists.add(NameScreeningResponse.HitsList.builder().fullName("mock name").build());
        }
        return NameScreeningResponse.builder()
                .hitsNumber(String.valueOf(hitNumber))
                .hitsList(hitsLists).
                build();

    }

    public static Identifier getIdentifier(String type, String value) {

        return Identifier.builder()
                .type(type)
                .value(value)
                .build();
    }

    public static CreditDecisionResponse getCreditDecisionResponse(String automaticDecision) {

        return CreditDecisionResponse.builder()
                .decisionResult(DecisionResult
                        .builder()
                        .automaticDecision(automaticDecision)
                        .build())
                .build();
    }


    public static BankGuaranteeData getBankGuaranteeData(boolean inScope, String subType) {
        return BankGuaranteeData.builder()
                .inScope(inScope)
                .subType(subType)
                .build();
    }

    public static List<Identifier> convertOnePamIdentifier(List<IdentifierOnePamResponse> involvedPartyInternalIdentifiers) {
        return Optional.ofNullable(involvedPartyInternalIdentifiers).
                orElse(Collections.emptyList()).
                stream().
                map(pamIdentifier -> Identifier.builder().
                        type(pamIdentifier.getInvolvedPartyInternalIdentifierType()).
                        value(pamIdentifier.getInvolvedPartyInternalIdentifierValue()).
                        build()).
                toList();
    }

    public static LegalRepresentativeData getLegalRepresentativeData(String uuid, String name, String email) {
        return LegalRepresentativeData.builder()
                .emailId(email)
                .fullName(name)
                .firstSigner(true)
                .uuid(uuid)
                .build();
    }

    public static Payment getPaymentDetails() {
        return Payment.builder()
                .referenceNumber(REFERENCE_NUMBER)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(SPECIFIED_TEXT)
                .bankGuaranteeEndType(BankGuaranteeEndType.UNSPECIFIED)
                .maturityDate(LocalDate.of(2099, 11, 30))
                .build();
    }

    public static BankGuaranteeRequest getBankGuaranteeRequest(String organisationId, String individualId, String sessionId, String requesterId) {
        return BankGuaranteeRequest.builder()
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .organisationId(organisationId)
                .status(BankGuaranteeRequestStatus.DRAFT)
                .bgRequest(BankGuaranteeRequestData.builder()
                        .stpResultDataSet(createStpResultDataset(BG_STP_RESULT_FILE)).build())
                .requestId(UUID.randomUUID().toString())
                .individualId(individualId)
                .sessionId(sessionId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static MoneyRetentionBond getMoneyRetentionBond() {
        return MoneyRetentionBond.builder()
                .advancePaymentIBAN(IBAN_ACCOUNT_BE)
                .referenceNumber(REFERENCE_NUMBER)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .maturityDate(LocalDate.of(2099, 11, 30))
                .build();
    }

    public static BidBond getBidBond() {
        return BidBond.builder()
                .referenceNumber(REFERENCE_NUMBER)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .maturityDate(LocalDate.of(2099, 11, 30))
                .build();
    }

    public static AdvancePayment getAdvancePaymentDetails() {
        return AdvancePayment.builder()
                .advancePaymentIBAN(IBAN_ACCOUNT_BE)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .contractDescription(CONTRACT_DESCRIPTION)
                .maturityDate(LocalDate.of(2099, 11, 30))
                .referenceNumber(REFERENCE_NUMBER)
                .build();
    }

    public static PerformanceBond getPerformanceBondDetails() {
        return PerformanceBond.builder()
                .referenceNumber(REFERENCE_NUMBER)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .maturityDate(LocalDate.of(2099, 11, 30))
                .finalMaturityDate(LocalDate.of(2099, 11, 30))
                .immediateMaturityDate(LocalDate.of(2099, 11, 30))
                .build();
    }

    public static GuaranteeDetailsData<Object> getGuaranteeDetailsData(BankGuaranteeCode bgCode, BaseGuaranteeType bgType) {
        return GuaranteeDetailsData.builder()
                .bgAmount(BigDecimal.valueOf(150))
                .bgCurrency(CURRENCY_EUR)
                .bgLanguage(BankGuaranteeLanguage.ENGLISH)
                .bgCode(bgCode).bankGuarantee(bgType)
                .build();
    }

    public static Rental getRentalDetails() {

        return Rental.builder()
                .city("Tokyo")
                .street("strt")
                .postalCode("1111TK")
                .country("BE")
                .rentalEndDate(LocalDate.now().plusDays(30))
                .referenceNumber("refNumber")
                .dateOfSignature(LocalDate.now().minusDays(10))
                .contractEndDate(LocalDate.now())
                .expiryDate(GUARANTEE_END_DATE)
                .gracePeriod("30")
                .build();
    }

    public static PublicContract getPublicContractDetails() {
        return PublicContract.builder()
                .tenderSpecification(true)
                .referenceNumber(REFERENCE_NUMBER)
                .grantDate(LocalDate.of(2099, 11, 30))
                .title(CONTRACT_DESCRIPTION)
                .totalAmount(TOTAL_AMOUNT)
                .expiryDate(GUARANTEE_END_DATE)
                .build();
    }

    public static WoodsBlankPromise getBlankPromiseDetails() {
        return WoodsBlankPromise.builder()
                .lotsDescription("BG_AMOUNT")
                .promiseEndDate(LocalDate.now().plusMonths(8L))
                .region(RegionCode.WALLONIA)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .build();
    }

    public static StateLottery getStateLotteryDetails() {
        return StateLottery.builder()
                .dateOfAgreeInPrinc(LocalDate.of(2099, 11, 30))
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static Ovam getOvamDetails() {
        return Ovam.builder()
                .referenceNumber(REFERENCE_NUMBER)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .wasteTransportEndDate(LocalDate.now())
                .wasteTransportStartDate(LocalDate.now())
                .contractDueDate(LocalDate.now().plusDays(30))
                .contractValidityEndDate(LocalDate.now().plusDays(365))
                .build();
    }

    public static CustomTypeOne getCustomOneDetails() {
        return CustomTypeOne.builder()
                .representing("representing")
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .amountPercentage(100)
                .build();
    }

    public static CustomTypeTwo getCustomTwoDetails() {
        return CustomTypeTwo.builder()
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static CustomTypeFour getCustomFourDetails() {
        return CustomTypeFour.builder()
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static CustomTypeFive getCustomFiveDetails() {
        return CustomTypeFive.builder()
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static Dck getDckDetails() {
        return Dck.builder()
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .contractDescription(CONTRACT_DESCRIPTION)
                .bankGuaranteeEnd("BankGuaranteeEnd")
                .buildingAddress(Dck.BuildingAddress.builder()
                        .city("CityName")
                        .postalCode("PostalCode")
                        .street("StreetNumber")
                        .build())
                .build();
    }

    public static RealEstate getRealEstateDetails() {
        return RealEstate.builder()
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }


    public static WoodsWALPublic getWoodsWalloniaPublicEntityDetails(CoverType coverType, boolean replacePromise) {
        return WoodsWALPublic.builder()
                .region(RegionCode.WALLONIA)
                .saleDate(LocalDate.now())
                .salePrice(BigDecimal.valueOf(500))
                .lotsDescription("lotsDescription")
                .replacePromise(replacePromise)
                .promiseIds(BG_REFERENCE_NUMBER)
                .bankGuaranteeEndType(BankGuaranteeEndType.UNSPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .firstTransactionAmt(BigDecimal.valueOf(1))
                .secondTransactionAmt(BigDecimal.valueOf(2))
                .firstDeadline(LocalDate.of(2099, 11, 30))
                .secondDeadline(LocalDate.of(2099, 11, 30))
                .residualAmount(BigDecimal.valueOf(100))
                .coverType(coverType)
                .build();
    }


    public static WoodsPrivate getWoodsWalloniaPrivateSaleDetails() {
        LocalDate now = LocalDate.now();
        LocalDate secondDeadline = now.plusDays(30);
        return WoodsPrivate.builder()
                .salePrice(BigDecimal.valueOf(10))
                .salePlace("SalePlace")
                .region(RegionCode.WALLONIA)
                .saleDate(LocalDate.now())
                .lotsDescription("lotsDescription")
                .salePrice(BigDecimal.valueOf(500))
                .replacePromise(true)
                .promiseIds(BG_REFERENCE_NUMBER)
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .firstTransactionAmt(BigDecimal.valueOf(1))
                .secondTransactionAmt(BigDecimal.valueOf(2))
                .firstDeadline(now)
                .secondDeadline(secondDeadline)
                .residualAmount(BigDecimal.valueOf(1))
                .maturityDate(secondDeadline.plusDays(365))
                .build();
    }

    public static WoodsVLAPublic getWoodsVLAPublicDetails() {
        LocalDate now = LocalDate.now();
        return WoodsVLAPublic.builder()
                .region(RegionCode.FLANDERS)
                .saleDate(LocalDate.now())
                .replacePromise(true)
                .promiseIds(BG_REFERENCE_NUMBER)
                .bankGuaranteeEndType(BankGuaranteeEndType.UNSPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .firstTransactionAmt(BigDecimal.valueOf(1))
                .secondTransactionAmt(BigDecimal.valueOf(2))
                .firstDeadline(now.plusDays(10))
                .secondDeadline(now.plusDays(20))
                .saleDate(now.plusDays(10))
                .salePlace("placeOfSale")
                .build();
    }

    public static GoodsTransport getGoodsTransport() {
        return GoodsTransport.builder()
                .bankGuaranteeEnd("bankguaranteeEnd")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .licenseNumber("licenseNumber")
                .noOfVehicle(12)
                .fodGuarantee(FodGuaranteeType.DCK)
                .bankGuaranteeId("BankGuaranteeId")
                .build();
    }

    public static PassengerTransport getPassengerTransport() {
        return PassengerTransport.builder()
                .bankGuaranteeEnd("bankguaranteeEnd")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .licenseNumber("licenseNumber")
                .noOfVehicle(12)
                .fodGuarantee(FodGuaranteeType.DCK)
                .build();
    }

    public static OperatorsTransport getTransportOperator() {
        return OperatorsTransport.builder()
                .bankGuaranteeEnd("bankguaranteeEnd")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .licenseNumber("licenseNumber")
                .noOfVehicle(12)
                .transportOperatorActivityType(TransportOperatorActivityType.FORWARDING_AGENT)
                .build();
    }

    public static WoodsDischarge getWoodsDischargeDetails(CoverType coverType, boolean replacePromise) {
        return WoodsDischarge.builder()
                .region(RegionCode.WALLONIA)
                .saleDate(LocalDate.now())
                .salePrice(BigDecimal.valueOf(10))
                .salePlace("placeOfSale")
                .lotsDescription("lotsDescription")
                .replacePromise(replacePromise)
                .promiseIds(BG_REFERENCE_NUMBER)
                .bankGuaranteeEndType(BankGuaranteeEndType.UNSPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .coverType(coverType)
                .build();
    }

    public static GuaranteeDetailsData<BaseGuaranteeType> getGuaranteeDetails(BankGuaranteeCode bankGuaranteeCode) {

        BaseGuaranteeType baseGuaranteeType = switch (bankGuaranteeCode) {
            case RENTAL -> getRentalDetails();
            case BID_BOND -> getBidBond();
            case ADVANCE_PAYMENT -> getAdvancePaymentDetails();
            case PAYMENT_GUARANTEE -> getPaymentDetails();
            case PUBLIC_CONTRACT -> getPublicContractDetails();
            case PERFORMANCE_BOND -> getPerformanceBondDetails();
            case STATE_LOTTERY -> getStateLotteryDetails();
            case MONEY_RETENTION_BOND -> getMoneyRetentionBond();
            case OVAM -> getOvamDetails();
            case REAL_ESTATE -> getRealEstateDetails();
            case CUSTOM_1 -> getCustomOneDetails();
            case CUSTOM_2 -> getCustomTwoDetails();
            case CUSTOM_4 -> getCustomFourDetails();
            case CUSTOM_5 -> getCustomFiveDetails();
            case DCK_CDC -> getDckDetails();
            case WOODS_PROM_A -> getStandardPromiseDetails();
            case WOODS_PROM_VLA -> getFlandersStandardPromiseDetails();
            case WOODS_PROM_B -> getBlankPromiseDetails();
            case WOODS_BGWAL_PUBLIC -> getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
            case WOODS_BG_PRIVATE -> getWoodsWalloniaPrivateSaleDetails();
            case WOODS_BG_DISCHARGE -> getWoodsDischargeDetails(CoverType.REMAINING_AMOUNT, true);
            case WOODS_BGVLA_PUBLIC -> getWoodsVLAPublicDetails();
            case GOODS_TRANSPORT -> getGoodsTransport();
            case PASSENGER_TRANSPORT -> getPassengerTransport();
            case OPERATORS_TRANSPORT -> getTransportOperator();
            case ABSTRACT_PROM -> getAbstractPromiseDetails();
            case PUBLIC_CONTRACT_PROM -> getPublicContractPromiseDetails();
            case CUSTOMIZED_TEXT -> getCustomizedTextDetails();
        };
        return GuaranteeDetailsData.<BaseGuaranteeType>builder()
                .bankGuarantee(baseGuaranteeType)
                .bgAmount(BigDecimal.valueOf(400))
                .bgCode(bankGuaranteeCode)
                .bgCurrency("003")
                .bgLanguage(BankGuaranteeLanguage.ENGLISH)
                .build();
    }


    public static GuaranteeDetailsPayload<BaseGuaranteeType> getGuaranteeDetailsPayload(BankGuaranteeCode bankGuaranteeCode) {

        BaseGuaranteeType baseGuaranteeType = switch (bankGuaranteeCode) {
            case RENTAL -> getRentalDetails();
            case BID_BOND -> getBidBond();
            case ADVANCE_PAYMENT -> getAdvancePaymentDetails();
            case PAYMENT_GUARANTEE -> getPaymentDetails();
            case PUBLIC_CONTRACT -> getPublicContractDetails();
            case PERFORMANCE_BOND -> getPerformanceBondDetails();
            case STATE_LOTTERY -> getStateLotteryDetails();
            case MONEY_RETENTION_BOND -> getMoneyRetentionBond();
            case OVAM -> getOvamDetails();
            case REAL_ESTATE -> getRealEstateDetails();
            case CUSTOM_1 -> getCustomOneDetails();
            case CUSTOM_2 -> getCustomTwoDetails();
            case CUSTOM_4 -> getCustomFourDetails();
            case CUSTOM_5 -> getCustomFiveDetails();
            case DCK_CDC -> getDckDetails();
            case WOODS_PROM_A -> getStandardPromiseDetails();
            case WOODS_PROM_VLA -> getFlandersStandardPromiseDetails();
            case WOODS_PROM_B -> getBlankPromiseDetails();
            case WOODS_BGWAL_PUBLIC -> getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
            case WOODS_BG_PRIVATE -> getWoodsWalloniaPrivateSaleDetails();
            case WOODS_BG_DISCHARGE -> getWoodsDischargeDetails(CoverType.REMAINING_AMOUNT, true);
            case WOODS_BGVLA_PUBLIC -> getWoodsVLAPublicDetails();
            case GOODS_TRANSPORT -> getGoodsTransport();
            case PASSENGER_TRANSPORT -> getPassengerTransport();
            case OPERATORS_TRANSPORT -> getTransportOperator();
            case ABSTRACT_PROM -> getAbstractPromiseDetails();
            case PUBLIC_CONTRACT_PROM -> getPublicContractPromiseDetails();
            case CUSTOMIZED_TEXT -> getCustomizedTextDetails();
        };
        return GuaranteeDetailsPayload.<BaseGuaranteeType>builder()
                .bankGuarantee(baseGuaranteeType)
                .bgAmount(BigDecimal.valueOf(400))
                .bgCode(bankGuaranteeCode)
                .bgTypesAmtCurrency("003")
                .bgLanguage(BankGuaranteeLanguage.ENGLISH)
                .build();
    }

    public static GuaranteeDetailsData<BaseGuaranteeType> getGuaranteeDetailsData(BankGuaranteeCode bankGuaranteeCode) {

        BaseGuaranteeType baseGuaranteeType = switch (bankGuaranteeCode) {
            case RENTAL -> getRentalDetails();
            case BID_BOND -> getBidBond();
            case ADVANCE_PAYMENT -> getAdvancePaymentDetails();
            case PAYMENT_GUARANTEE -> getPaymentDetails();
            case PUBLIC_CONTRACT -> getPublicContractDetails();
            case PERFORMANCE_BOND -> getPerformanceBondDetails();
            case STATE_LOTTERY -> getStateLotteryDetails();
            case MONEY_RETENTION_BOND -> getMoneyRetentionBond();
            case OVAM -> getOvamDetails();
            case REAL_ESTATE -> getRealEstateDetails();
            case CUSTOM_1 -> getCustomOneDetails();
            case CUSTOM_2 -> getCustomTwoDetails();
            case CUSTOM_4 -> getCustomFourDetails();
            case CUSTOM_5 -> getCustomFiveDetails();
            case DCK_CDC -> getDckDetails();
            case WOODS_PROM_A -> getStandardPromiseDetails();
            case WOODS_PROM_VLA -> getFlandersStandardPromiseDetails();
            case WOODS_PROM_B -> getBlankPromiseDetails();
            case WOODS_BGWAL_PUBLIC -> getWoodsWalloniaPublicEntityDetails(CoverType.CASH, true);
            case WOODS_BG_PRIVATE -> getWoodsWalloniaPrivateSaleDetails();
            case WOODS_BG_DISCHARGE -> getWoodsDischargeDetails(CoverType.REMAINING_AMOUNT, true);
            case WOODS_BGVLA_PUBLIC -> getWoodsVLAPublicDetails();
            case GOODS_TRANSPORT -> getGoodsTransport();
            case PASSENGER_TRANSPORT -> getPassengerTransport();
            case OPERATORS_TRANSPORT -> getTransportOperator();
            case ABSTRACT_PROM -> getAbstractPromiseDetails();
            case PUBLIC_CONTRACT_PROM -> getPublicContractPromiseDetails();
            case CUSTOMIZED_TEXT -> getCustomizedTextDetails();
        };
        return GuaranteeDetailsData.<BaseGuaranteeType>builder()
                .bankGuarantee(baseGuaranteeType)
                .bgAmount(BigDecimal.valueOf(400))
                .bgCode(bankGuaranteeCode)
                .bgCurrency("003")
                .bgLanguage(BankGuaranteeLanguage.ENGLISH)
                .build();
    }

    public static BeneficiaryDocumentPayload getBeneficiaryDocumentPayload() {
        return BeneficiaryDocumentPayload.builder()
                .companyName("mock company name")
                .kboNumber("1234.567.890")
                .street("street")
                .city("city")
                .zip("440003")
                .country("Belgium")
                .build();
    }


    public static ConnectDotInput getConnectDotInput(BankGuaranteeRequestData requestData, DocumentType documentType) {
        return ConnectDotInput.builder()
                .bankGuaranteeRequestData(requestData).creditType(CreditType.ISOLATED)
                .documentType(documentType).garOutput(GarOutput.builder()
                        .collaterals(List.of(GarOutput.Collateral.builder()
                                .assertDescription("ASSERTDESC")
                                .collateralType("COLLATERALTYPE")
                                .comments("COMMENTS")
                                .grantedBy(List.of(GarOutput.Collateral.GarParty.builder()
                                        .name("NAME")
                                        .build()))
                                .garAmount(GarOutput.GarAmount.builder()
                                        .value(TOTAL_AMOUNT)
                                        .currency(CURRENCY_EUR)
                                        .build()).build()))
                        .covenants(List.of(GarOutput.Covenant.builder()
                                .agreementType("AGREEMENTTYPE")
                                .comments("COMMENTS")
                                .generalCoverType("COVERTYPE")
                                .grantedBy(List.of(GarOutput.Collateral.GarParty.builder()
                                        .name("NAME")
                                        .build()))
                                .garAmount(GarOutput.GarAmount.builder()
                                        .currency(CURRENCY_EUR)
                                        .value(TOTAL_AMOUNT)
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static BankGuaranteeRequestData getBankGuaranteeRequestData(GuaranteeDetailsData guaranteeDetails, BankGuaranteeRecipient deliveryInformationRecipient, boolean isIssueToAnotherParty) {

        BeneficiaryPostalAddressData postAddressData = BeneficiaryPostalAddressData.builder()
                .cityName("CITY")
                .firstAddress("FIRSTADDRESS")
                .postalCode("POSTCODE")
                .streetName("STREETNAME")
                .build();

        ApplicantPostalAddressData applicantPostalAddressData = ApplicantPostalAddressData.builder()
                .cityName("CITY")
                .firstAddress("FIRSTADDRESS")
                .postalCode("POSTCODE")
                .streetName("STREETNAME")
                .build();

        return BankGuaranteeRequestData.builder()
                .dossierInformation(DossierData.builder()
                        .agreementDossierResponseId(DOSSIER_ID)
                        .dossierResponseId(DOCUMENT_ID).build())
                .referenceNumber(REFERENCE_NUMBER)
                .financialInformation(FinancialInformationData.builder()
                        .accountToBeDebited(FinancialInformationData.AccountData.builder()
                                .accountCurrency(CURRENCY_EUR)
                                .accountName(ACCOUNT_NAME)
                                .ibanNumber(IBAN_ACCOUNT_BE)
                                .uuid(ACCOUNT_UUID)
                                .balanceAmount(BigDecimal.valueOf(2222)).build())
                        .creditLine(FinancialInformationData.ContractDetailData.builder()
                                .accountName(ACCOUNT_NAME)
                                .uuid(ACCOUNT_UUID)
                                .amount(TOTAL_AMOUNT)
                                .ibanNumber(IBAN_ACCOUNT_BE)
                                .creditLineAccountNumber(BigDecimal.valueOf(12345678))
                                .build()).build())
                .guaranteeDetails(guaranteeDetails)
                .deliveryInformation(DeliveryInformationData.builder()
                        .recipient(deliveryInformationRecipient)
                        .mode(BankGuaranteeDeliveryMode.COURIER_SERVICE)
                        .build()).applicant(ApplicantData.builder()
                        .cinNumber(CIN_NUMBER)
                        .organisationName(ApplicantData.ApplicantOrganisationNameData.builder()
                                .fullName(LEGAL_ENTITY_FULLNAME)
                                .build()).postalAddress(applicantPostalAddressData)
                        .build())
                .beneficiary(BeneficiaryData.builder()
                        .cinNumber(CIN_NUMBER)
                        .organisationName(BeneficiaryOrganisationNameData.builder()
                                .fullName(LEGAL_ENTITY_FULLNAME)
                                .build()).postalAddress(postAddressData)
                        .emailAddress(BeneficiaryData.BeneficiaryEmailData.builder()
                                .emailIdInformation("Beneficiary Email id")
                                .build()).build())
                .legalRepresentatives(getLegalRepresentatives())
                .issueToAnotherParty(isIssueToAnotherParty)
                .instructingParty(InstructingPartyData.builder()
                        .individual(InstructingPartyData.IndividualData.builder()
                                .legalRepId(LEGAL_REP_ID)
                                .individualName(InstructingPartyData.IndividualNameData.builder()
                                        .fullName(FULL_NAME)
                                        .build())
                                .internalIdentifiers(List.of(Identifier.builder()
                                        .type("CSI_BE")
                                        .value("VALUE")
                                        .build()))
                                .build())
                        .organisation(InstructingPartyData.OrganisationData.builder()
                                .cinNumber(CIN_NUMBER)
                                .organisationName(InstructingPartyData.OrganisationNameData.builder()
                                        .fullName(ORG_FULL_NAME)
                                        .type(ORG_TYPE)
                                        .build())
                                .postalAddress(InstructingPartyData.PostalAddressData.builder()
                                        .firstAddress("FIRSTADDRESS")
                                        .cityName("CITYNAME")
                                        .build())
                                .internalIdentifiers(List.of(Identifier.builder()
                                        .type("CSI_BE")
                                        .value("VALUE")
                                        .build()))
                                .build())
                        .build())
                .translationLanguage(Locale.ENGLISH)
                .build();
    }

    public static List<LegalRepresentativeData> getLegalRepresentatives() {
        return List.of(LegalRepresentativeData.builder()
                .uuid(UUID.randomUUID().toString())
                .firstSigner(true).fullName(LEGAL_REP_FULL_NAME_1)
                .emailId(LEGAL_REP_EMAIL_1).build(), LegalRepresentativeData.builder()
                .uuid(UUID.randomUUID().toString()).firstSigner(false).fullName(LEGAL_REP_FULL_NAME_2)
                .emailId(LEGAL_REP_EMAIL_2)
                .build());
    }


    public static PamQualificationResponse getPamQualificationResponse() {

        return PamQualificationResponse.builder()
                .beneficiary(PartyResponse.builder()
                        .subResults(List.of())
                        .status("Yes")
                        .build())
                .build();
    }

    public static BankAccountNumberResponse getBankAccountNumberResponse() {
        return BankAccountNumberResponse.builder()
                .accountNumber("accountNumber")
                .accountType("accountType")
                .build();
    }

    public static AccountLoanResponse getAccountLoanResponse() {
        return AccountLoanResponse.builder()
                .facilityAgreement(List.of(AccountLoanResponse.FacilityAgreementItem.builder()
                        .lendingLimitAmount(AccountLoanResponse.FacilityAgreementItem.LendingLimitAmount.builder()
                                .value(new BigDecimal(123456)).build()).build()))
                .build();
    }

    public static Document getDocument(String organisationId, String documentId, String requesterId) {
        return Document.builder()
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .organisationId(organisationId)
                .status(DocumentStatus.NEW)
                .requestId(UUID.randomUUID().toString())
                .documentId(documentId)
                .documentType(DocumentType.BG_DRAFT)
                .build();
    }

    public static InstructingPartyPayload.DigitalAddressPayload getDigitalAddress(String fullAddress, String type) {
        return InstructingPartyPayload.DigitalAddressPayload.builder()
                .digitalAddressType(type)
                .fullDigitalAddress(fullAddress)
                .build();
    }

    public static BankGuaranteeRequestPayload createBankGuaranteeRequestPayload(String fileName) {
        return mapFileToObject(fileName, BankGuaranteeRequestPayload.class);
    }

    public static BankGuaranteeRequestData createBankGuaranteeRequestData(String fileName) {
        return mapFileToObject(fileName, BankGuaranteeRequestData.class);
    }

    public static StpResultDataSet createStpResultDataset(String fileName) {
        return mapFileToObject(fileName, StpResultDataSet.class);
    }

    public static BankGuaranteeDataSet createBankGuaranteeDataset(String fileName) {
        return mapFileToObject(fileName, BankGuaranteeDataSet.class);
    }

    public static InvolvedPartyOnePamResponse createOrganizationOnePamResponse(String fileName) {
        return mapFileToObject(fileName, InvolvedPartyOnePamResponse.class);
    }

    public static InvolvePartyData createInvolvePartyDataResponse(String fileName) {
        return mapFileToObject(fileName, InvolvePartyData.class);
    }

    public static InvolvedPartyOnePamResponse createIndividualOnePamResponse(String fileName) {
        return mapFileToObject(fileName, InvolvedPartyOnePamResponse.class);
    }

    public static GranteeGrantorResponse createGranteeResponse(String fileName) {
        return mapFileToObject(fileName, GranteeGrantorResponse.class);
    }

    public static List<CountryDataCache> createCountryDataset(String fileName) {
        return JsonUtils.mapFileToCollection(fileName, new TypeReference<>() {
        });
    }

    public static DarListResponse.DarResponse getDarResponse(String darId) {
        return DarListResponse.DarResponse.builder()
                .darUuid(darId)
                .build();
    }

    public static BgRequestDataLakeEventDto getBgRequestDataLakeDto(BankGuaranteeRequest bankGuaranteeRequest) {

        return BgRequestDataLakeEventDto.builder()
                .sessionId(bankGuaranteeRequest.getSessionId())
                .filter(CommonUtils.isSTP(bankGuaranteeRequest.getBgRequest().getStpResultDataSet()))
                .eventName(getDataLakeEventName(bankGuaranteeRequest.getStatus()))
                .traceId(TracingHelper.getINGSpanContext().getTraceId())
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .confidentiality(3)
                .version("1")
                .build();
    }

    public static BankGuaranteeFinalizationPayload getBankGuaranteeFinalizationPayload(String requestId) {

        return BankGuaranteeFinalizationPayload.builder()
                .requestId(requestId)
                .build();
    }

    public static BankGuaranteesDataLakeEvent createBankGuaranteeRequestDataLake(String fileName) {
        return mapFileToObject(fileName, BankGuaranteesDataLakeEvent.class);
    }

    public static NotificationPropertiesDetails getNonStpNotificationPropertyDetail() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .attachmentContentType("text/xml")
                .attachmentFileName("%s_%s.xml")
                .ccAddresses(List.of("testcc@ing.com"))
                .recipientAddress("recipant@ing.com")
                .senderAddress("noreply@ing.com")
                .attachmentTemplateReference("P31464/Template/simple-xml-document")
                .templateReference("P31464/Template/simple-html-document")
                .channel("email")
                .build();
    }

    public static NotificationPropertiesDetails getStpResultsPropertiesDetail() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .ccAddresses(List.of("testcc@ing.com"))
                .recipientAddress("recipant@ing.com")
                .senderAddress("noreply@ing.com")
                .templateReference("P31464/Template/stp-results-html-document")
                .channel("email")
                .build();
    }

    public static NotificationPropertiesDetails getSignCompletionPropertiesDetail() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .ccAddresses(List.of("testcc@ing.com"))
                .recipientAddress("recipant@ing.com")
                .senderAddress("noreply@ing.com")
                .templateReference("P31464/Template/signature-completion-html-document")
                .channel("email")
                .build();
    }

    public static NotificationPropertiesDetails getBeneficiaryPropertyDetail() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .attachmentContentType("text/xml")
                .attachmentFileName("%s_%s.xml")
                .ccAddresses(new ArrayList<>(List.of("testcc@ing.com")))
                .recipientAddress("sophie.dandoy@loterie-nationale.be")
                .senderAddress("noreply@ing.com")
                .attachmentTemplateReference("P31464/Template/simple-xml-document")
                .templateReference("P31464/Template/simple-html-document")
                .aedEndpoint("aedEndpoint")
                .attachmentUrl("attachementUrl")
                .channel("email")
                .build();
    }

    public static EmailNotificationInput getNonStpNotificationInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documents) {
        SpanContext ingSpanContext = TracingHelper.getINGSpanContext();
        return EmailNotificationInput.builder()
                .spanId(ingSpanContext.getSpanId())
                .traceId(ingSpanContext.getTraceId())
                .profileId(bankGuaranteeRequest.getIndividualId())
                .sessionId(bankGuaranteeRequest.getSessionId())
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .documentList(documents)
                .build();
    }

    public static EngagementSuiteNotificationEvent creteEngagementSuiteNotificationEvent(String fileName) {
        return mapFileToObject(fileName, EngagementSuiteNotificationEvent.class);
    }

    public static NotificationEvent creteNotificationEvent(String fileName) {
        return mapFileToObject(fileName, NotificationEvent.class);
    }

    @SneakyThrows
    public static ExternalFeedbackEvent externalFeedbackEvent(String fileName) {
        ExternalFeedbackEvent externalFeedbackEvent = mapFileToObject(fileName, ExternalFeedbackEvent.class);
        externalFeedbackEvent.getNotificationFeedback().setTimestamp(Instant.now());
        return externalFeedbackEvent;
    }

    public static PegaCreateCaseRequest createPegaPayload(String fileName) {
        return mapFileToObject(fileName, PegaCreateCaseRequest.class);
    }

    public static PegaCreateCaseInput getPegaCaseInput(Document document, BankGuaranteeRequest bankGuaranteeRequest,
                                                       PegaCaseType pegaCaseType) {
        return PegaCreateCaseInput.builder()
                .documents(List.of(document))
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .pegaCaseType(pegaCaseType)
                .build();
    }

    public static PegaCreateCaseResponse getPegaResponse() {
        return PegaCreateCaseResponse.builder()
                .pxObjClass("Pega-API-CaseManagement-Case")
                .id("ING-WB-TFS-WORK TFS-BG-53634")
                .build();
    }

    public static RemoteDocumentStatusResponse getRemoteDocumentStatusResponse() {
        return RemoteDocumentStatusResponse.builder()
                .status(DocumentStatus.READY)
                .build();
    }

    public static DarInput getDarInput(BankGuaranteeRequestData bankGuaranteeRequestData, List<Document> documentDTOS) {
        return DarInput.builder()
                .bankGuaranteeRequestData(bankGuaranteeRequestData)
                .documents(documentDTOS)
                .build();
    }

    public static DarListResponse getDarListResponse(String darId) {
        return DarListResponse.builder()
                .data(List.of(DarListResponse.DarResponse.builder()
                        .darUuid(darId)
                        .build()))
                .build();
    }

    public static CreditDecisionInput getCreditRiskScoreInput() {
        return CreditDecisionInput.builder()
                .accessToken(getAccessToken())
                .organisationId(PamRequestIdentifier.builder()
                        .type(CSI)
                        .value("123456789")
                        .build())
                .individualId(PamRequestIdentifier.builder()
                        .type(CSI)
                        .value("999999999999")
                        .build())
                .requestId(CommonUtils.getRandomRequestId(false))
                .amount(BigDecimal.valueOf(3_124_545.34))
                .build();
    }


    public static SignDocumentPayload getSignDocumentPayload(String requestId) {
        return SignDocumentPayload.builder()
                .requestId(UUID.randomUUID().toString())
                .build();
    }

    public static ConnectDotInput getConnectDotInput(BankGuaranteeRequestData bankGuaranteeRequestData) {
        return ConnectDotInput.builder()
                .bankGuaranteeRequestData(bankGuaranteeRequestData)
                .documentType(DocumentType.BG_DRAFT)
                .build();
    }


    public static ReferenceDataMultilingualResponse getReferenceDataMLResponse() {

        return ReferenceDataMultilingualResponse.builder()
                .data(List.of(ReferenceData.builder()
                                .businessKey("BE")
                                .language("EN")
                                .translation("Belgium")
                                .build(),
                        ReferenceData.builder()
                                .businessKey("PT")
                                .language("FR")
                                .translation("Portugal")
                                .build()))
                .build();
    }

    public static ReferenceDataAttributeOutput getReferenceDataAttributesOutput() {

        return ReferenceDataAttributeOutput.builder().businessKeyList(List.of("BE", "PT")).build();
    }

    public static ReferenceDataAttributeResponse createCountryAttributeResponse(String fileName) {
        return mapFileToObject(fileName, ReferenceDataAttributeResponse.class);
    }

    public static GuaranteeDetailsData<PerformanceBond> getGuaranteeDetailsDataForPerformanceBond(BankGuaranteeRequestData bankGuaranteeRequestData) {
        GuaranteeDetailsData<PerformanceBond> guaranteeDetailsData = new GuaranteeDetailsData<>();
        guaranteeDetailsData.setBgAmount(bankGuaranteeRequestData.getGuaranteeDetails().getBgAmount());
        guaranteeDetailsData.setBgCode(BankGuaranteeCode.PERFORMANCE_BOND);
        guaranteeDetailsData.setBgCurrency(bankGuaranteeRequestData.getGuaranteeDetails().getBgCurrency());
        guaranteeDetailsData.setBgLanguage(bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage());
        guaranteeDetailsData.setBankGuarantee(PerformanceBond.builder()
                .contractDescription("contractDescription")
                .bankGuaranteeEnd("BankGuaranteeEnd")
                .referenceNumber("referenceNumber")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .maturityDate(LocalDate.of(2025, 8, 30))
                .finalMaturityDate(LocalDate.of(2025, 8, 30))
                .immediateMaturityDate(LocalDate.of(2025, 8, 30))
                .build());
        return guaranteeDetailsData;
    }

    public static FulfilmentInput getFulfilmentInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documentList) {
        return FulfilmentInput.builder()
                .documents(documentList)
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .build();
    }

    public static IntakeApiResponse getIntakeApiResponse(String masterReferenceId, String pegaCaseId) {
        return IntakeApiResponse.builder()
                .masterReference(masterReferenceId)
                .pegaCaseId(pegaCaseId)
                .id("BEBG1236419951")
                .build();
    }

    public static IntakeApiInput getIntakeInput(BankGuaranteeRequest bankGuaranteeRequest, List<Document> documentList) {
        return IntakeApiInput.builder()
                .bankGuaranteeRequest(bankGuaranteeRequest)
                .documents(documentList)
                .build();
    }

    public static CallbackEvent getCallbackEvent(String requestId, String darStatus, String darId) {
        CallbackEvent callbackEvent = new CallbackEvent();
        callbackEvent.setAgreementId(requestId);
        callbackEvent.setStatus(darStatus);
        callbackEvent.setDarUuid(darId);
        return callbackEvent;
    }

    public static IntakeApiRequest createIntakeApiPayload(String fileName) {
        return mapFileToObject(fileName, IntakeApiRequest.class);
    }

    public static UploadDocumentInput getUploadDocumentInput(String documentId, DocumentType documentType) {
        return UploadDocumentInput.builder()
                .documentId(documentId)
                .documentType(documentType)
                .language("en")
                .fileContent(new ByteArrayResource("Document".getBytes()))
                .build();
    }

    public static GessSignInput geGessSignInput(String email, String base64Document,
                                                DocumentType documentType,
                                                BankGuaranteeCode bankGuaranteeCode) {
        return GessSignInput.builder()
                .requesterId(BigInteger.valueOf(123445678))
                .requesterEmail(email)
                .requesterSystemId("123456456")
                .documentBase64(base64Document)
                .fileName("mockfile.pdf")
                .documentType(documentType)
                .bankGuaranteeCode(bankGuaranteeCode)
                .build();
    }


    public static GessProperties getGessProperties() {
        GessProperties gessProperties = new GessProperties();
        gessProperties.setSignPosition(SignPosition.BOTTOM_LEFT);
        gessProperties.setCoordinateConfig(getGessCoordinateConfigProperties());
        return gessProperties;

    }

    public static GessCoordinateConfigProperties getGessCoordinateConfigProperties() {

        return GessCoordinateConfigProperties.builder()
                .abstractModel(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .customFourFrench(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .customFourDutch(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .publicContract(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .stateLottery(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .contractLetter(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .customFive(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .customTwo(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .customOne(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .ovam(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .rental(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .realEstate(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .woods(Map.of("first", List.of(10, 460, 150, 50, 2), "second", List.of(10, 460, 150, 50, 2)))
                .build();
    }

    public static CreditLineArrangementOutput getCreditLineArrangementOutput() {
        return CreditLineArrangementOutput.builder().status("OK").
                build();
    }

    public static BookingRequest getBookingRequest() {
        return BookingRequest.builder()
                .reservationNumber("12345")
                .requestType(RequestType.CREATE)
                .accountNumber(BigDecimal.valueOf(12345L))
                .klcNumber(BigDecimal.valueOf(1234L))
                .build();
    }

    public static BookingResponse getBookingResponse() {
        return BookingResponse.builder()
                .dataSource("datasource")
                .eventIdentifier("eventIdentifier")
                .mutationCode(12)
                .agreementIdentifier(BookingResponse.AgreementIdentifier.builder()
                        .type("type")
                        .value("value")
                        .build())
                .build();
    }

    public static BaseGuaranteeType getStandardPromiseDetails() {
        return WoodsWalloniaStandardPromise.builder()
                .region(RegionCode.WALLONIA)
                .saleDate(LocalDate.now())
                .salePlace("Belgium")
                .promiseEndDate(LocalDate.now().plusMonths(4L))
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .build();
    }

    public static BaseGuaranteeType getFlandersStandardPromiseDetails() {
        return WoodsFlandersStandardPromise.builder()
                .region(RegionCode.FLANDERS)
                .saleDate(LocalDate.now())
                .salePlace("Belgium")
                .promiseEndDate(LocalDate.now().plusMonths(8L))
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .build();
    }

    public static BaseGuaranteeType getAbstractPromiseDetails() {
        return PromiseAbstract.builder()
                .contractDescription("description")
                .referenceNumber("masterReferenceId")
                .promiseEndDate(LocalDate.now().plusMonths(6L))
                .bankGuaranteeEnd("bank guarantee end")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static BaseGuaranteeType getPublicContractPromiseDetails() {
        return PromisePublicContract.builder()
                .title("title of the public contract")
                .contractDescription("masterReferenceId")
                .promiseEndDate(LocalDate.now().plusMonths(6L))
                .bankGuaranteeEnd("bank guarantee end")
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .build();
    }

    public static BaseGuaranteeType getCustomizedTextDetails() {
        return CustomizedText.builder()
                .bankGuaranteeEndType(BankGuaranteeEndType.SPECIFIED)
                .bankGuaranteeEnd(GUARANTEE_END_DATE)
                .contractDescription("Contract Description")
                .comments("masterComments")
                .maturityDate(LocalDate.now().plusDays(30))
                .otherOptionForPartial("other")
                .referenceNumber("masterReferenceId")
                .subBgCode(BankGuaranteeCode.RENTAL)
                .build();
    }


    public static KfcCreditOverviewProperties getKfcCreditOverviewProperties() {
        KfcCreditOverviewProperties kfcCreditOverviewProperties = new KfcCreditOverviewProperties();
        kfcCreditOverviewProperties.setLevel(21);
        kfcCreditOverviewProperties.setEmployeeIdType("MATRICULE");
        kfcCreditOverviewProperties.setEmployeeIdValue("841020413");
        kfcCreditOverviewProperties.setInvolvePartyType("ACC");
        return kfcCreditOverviewProperties;
    }

    public static CustomerArrangementResponse getCustomerArrangementResponse() {
        return CustomerArrangementResponse.builder()
                .signaleticInformation(List.of(SignaleticInformationResponse.builder()
                        .codeLanguage(1)
                        .segMis(228)
                        .codeXy(29)
                        .contraNotariety(0)
                        .respCommercialManagerNo(1818)
                        .loanStatusManual(20)
                        .ssomi("03846-Person T900810708")
                        .gridId(String.valueOf(41803507))
                        .build()))
                .build();
    }

    public static List<CreditBalanceOutput> getCreditBalanceOutput(BigDecimal accountNumber, BigDecimal amount) {

        return List.of(CreditBalanceOutput.builder()
                .rangeStatus(50)
                .productCode(6000)
                .creditLineAccountNumber(accountNumber)
                .availableAmount(CreditBalanceOutput.AvailableAmountOutput.builder()
                        .amount(amount)
                        .build())
                .build());

    }

    public static CustomerArrangementOutput getCustomerArrangementOutput() {

        return CustomerArrangementOutput.builder()
                .codeLanguage(1)
                .segMis(228)
                .codeXy(29)
                .contraNotariety(0)
                .respCommercialManagerNo(1818)
                .loanStatusManual(20)
                .ssomi("03846-Person T900810708")
                .gridId(String.valueOf(41803507))
                .csiIdentifier(String.valueOf(238035471L))
                .itvIdentifier(String.valueOf(727226123L))
                .build();

    }

    public static DefaultBeneficiaryResponse getDefaultBeneficiaryResponse() {

        return DefaultBeneficiaryResponse.builder()
                .cityName("cityname")
                .countryCode("228")
                .firstAddress("first Address")
                .postalCode("postalcode")
                .email("email")
                .pageDescription("Ovam")
                .name("name")
                .build();

    }

    public static BeneficiaryProperties.BeneficiaryDetails getBeneficiaryDetails() {
        return BeneficiaryProperties.BeneficiaryDetails.builder()
                .cityName("cityname")
                .firstAddress("first Address")
                .name("name")
                .build();
    }

    public static BeneficiaryProperties.BeneficiaryData getBeneficiaryData() {

        HashMap<String, BeneficiaryProperties.BeneficiaryDetails> map = new HashMap<>();
        map.put("en", MockHelper.getBeneficiaryDetails());
        return BeneficiaryProperties.BeneficiaryData.builder()
                .countryCode("228")
                .postalCode("postalcode")
                .email("email")
                .pageDescription("Ovam")
                .beneficiaryDetails(map)
                .build();

    }

    public static SignatoriesSelectionRequest getSignatorySelectionRequest(String legalEntityId, GuaranteeDetailsData<?> guaranteeDetailsData) {
        return SignatoriesSelectionRequest.builder()
                .legalEntityId(legalEntityId)
                .guaranteeDetails(guaranteeDetailsData)
                .build();
    }

    public static AlerSignatories createAlerSignatories(String fileName) {
        return mapFileToObject(fileName, AlerSignatories.class);
    }

    public static AlerRetrieveSignatoriesResponse createAlerRetrieveSignatoriesResponse(String fileName) {
        return mapFileToObject(fileName, AlerRetrieveSignatoriesResponse.class);
    }


    public static AlerSignatories getAlerSignatories(String transactionId, String transactionStatus,
                                                     List<Signatory> signatories, AlerErrorDetails alerErrorDetails) {
        return AlerSignatories.builder()
                .transactionId(transactionId)
                .transactionStatus(transactionStatus)
                .errorDetails(alerErrorDetails)
                .signatories(signatories)
                .build();
    }

    public static AlerRetrieveSignatoriesResponse getAlerRetrieveSignatoriesResponse(String transactionId, String transactionStatus,
                                                                                     SignatoryResponse signatoryResponse, ErrorResponse errorResponse) {
        return AlerRetrieveSignatoriesResponse.builder()
                .transactionId(transactionId)
                .transactionStatus(transactionStatus)
                .errorResponseDetails(errorResponse)
                .signatoryResponse(signatoryResponse)
                .build();
    }


    public static AlerRetrieveSignatoriesInput getAlerRetrieveSignatoriesInput(AccessToken accessToken, String legalEntityId) {
        return AlerRetrieveSignatoriesInput.builder()
                .organisationId(legalEntityId)
                .accessToken(accessToken)
                .translation(Locale.ENGLISH)
                .build();
    }

    public static FxRatesConversionInput getFxReteConversionInput(BigDecimal amount, String fromCurrency, String toCurrency) {
        return FxRatesConversionInput.builder()
                .amount(amount)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .build();
    }

    public static FxRateConversionResponse getFxRateConversionResponse(BigDecimal amount) {
        return FxRateConversionResponse.builder()
                .convertedAmount(amount)
                .build();
    }

    public static FulfillmentProperties getFulfilmentProperties() {
        FulfillmentProperties fulfillmentProperties = new FulfillmentProperties();
        Map<String, String> errors = new HashMap<>();
        errors.put("BGOS-19", "Technical error occurs while signing the bank guarantee by bank");
        errors.put("BGOS-18", "Technical error occurs in sending bank guarantee to Ti through intake");
        errors.put("BGOS-17", "Technical error occurs in fund reservation");
        errors.put("BGOS-11", "Technical error occurs while fetching the gar collaterals");
        errors.put("BGOS-09", "Technical error occurs while generating the final documents");
        errors.put("BGOS-08", "Technical error occurs in documentum while creating place holders");
        fulfillmentProperties.setErrors(errors);
        return fulfillmentProperties;
    }

    public static DossierUpdateData getDossierUpdateData(String legalEntityId, String requestId, String documentId) {
        return DossierUpdateData.builder()
                .documentId(documentId)
                .legalEntityId(legalEntityId)
                .requestId(requestId)
                .build();
    }

    public static CustomDocument getCustomDocument(String legalEntityId, String dossierid, String agreementId,
                                                   String requestId, String documentId, String requesterId) {
        return CustomDocument.builder()
                .documentId(documentId)
                .agreementDossierId(agreementId)
                .dossierId(dossierid)
                .requestId(requestId)
                .status(CustomDocumentStatus.NEW)
                .organisationId(legalEntityId)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .build();
    }

    public static List<BankHoliday> getBankHolidayList() {
        return List.of(BankHoliday.builder()
                .date(LocalDate.now().plusDays(2))
                .build(), BankHoliday.builder()
                .date(LocalDate.now().plusDays(3))
                .build());
    }

    public static DetailedCallbackEvent getIntermediateCallbackEvent(String requestId, String darStatus,String operation, String darId) {
        DetailedCallbackEvent callbackEvent = new DetailedCallbackEvent();
        DarInfo darInfo = new DarInfo();
        darInfo.setAgreementId(requestId);
        darInfo.setStatus(darStatus);
        darInfo.setDarUuid(darId);
        callbackEvent.setDarInfo(darInfo);
        callbackEvent.setOperations(List.of(operation));
        return callbackEvent;
    }

    public static DarProperties getDarProperties() {
        DarProperties darProperties = new DarProperties();
        darProperties.setBusinessLine("ING Bank Guarantee Team");
        darProperties.setCancelUrl("/banking/be-bankguarantee?flow-step=cancel-page");
        darProperties.setCloseUrl("/banking/be-bankguarantee?flow-step=close-page");
        darProperties.setFrontendCallbackUrl("/banking/be-bankguarantee");
        darProperties.setDocumentSignCallback("bgos-docsign-callback");
        darProperties.setDocumentUriPrefix("/case-management/documents/%s");
        darProperties.setExpiryMessageEn("You can submit a new bank guarantee request via the ING Bank Guarantees Portal.");
        darProperties.setExpiryMessageFr("Vous pouvez introduire une nouvelle demande de garantie bancaire via le Portail des Garanties Bancaires ING.");
        darProperties.setExpiryMessageNl("Een nieuwe aanvraag voor bankgarantie kan je indienen via ING Bankgaranties Portaal.");
        darProperties.setTribe("Business lending");
        return darProperties;

    }

    public static NotificationPropertiesDetails getRequesterPartiallySignedPropertiesDetails() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .ccAddresses(List.of("testcc@ing.com"))
                .recipientAddress("recipant@ing.com")
                .senderAddress("noreply@ing.com")
                .templateReference("P31464/Template/requester-partially-signed-html-document")
                .channel("email")
                .build();
    }

    public static NotificationPropertiesDetails getSignatureReminderPropertiesDetails() {

        return NotificationPropertiesDetails.builder()
                .topic("EngagementSuite_OperationalNotificationEvent")
                .ccAddresses(List.of("testcc@ing.com"))
                .recipientAddress("recipant@ing.com")
                .senderAddress("noreply@ing.com")
                .templateReference("P31464/Template/signature-reminder-html-document")
                .channel("email")
                .build();
    }
}