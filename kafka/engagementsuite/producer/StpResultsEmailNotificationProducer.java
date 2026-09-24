package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.DossierData;
import com.ing.bankguarantees.models.domain.InstructingPartyData;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.getSource;
import static com.ing.bankguarantees.utils.CommonUtils.getIdentifierValue;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class StpResultsEmailNotificationProducer {

    private static final String AGREEMENT_DOSSIER_TYPE_CODE = "BLEBGAD01";

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    @Value("${bgos.dossier-url}")
    private String dossierUrl;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing non stp notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getStpResultsEmailNotificationProperties().getTopic(),
                createNotificationCallback(notificationEvent, notificationProperties.getStpResultsEmailNotificationProperties()));
    }


    private EmailNotificationCallback createNotificationCallback(NotificationEvent notificationEvent,
                                                                 NotificationPropertiesDetails stpResultsNotificationPropertiesDetails) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(stpResultsNotificationPropertiesDetails)
                .build();
    }

    private NotificationEvent createNotificationEvent(EmailNotificationInput emailNotificationInput) {
        return NotificationEvent.builder()
                .header(createHeader(emailNotificationInput))
                .body(createBody(emailNotificationInput))
                .attachments(Collections.emptyList())
                .build();
    }


    private Header createHeader(EmailNotificationInput emailNotificationInput) {
        return Header.builder()
                .id(java.util.UUID.randomUUID().toString())
                .traceId(emailNotificationInput.getTraceId())
                .created(Instant.now().toEpochMilli())
                .source(getSource())
                .metadata(NotificationHelper.prepareMetadataParam(emailNotificationInput))
                .build();
    }


    private NotificationEventBody createBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getStpResultsEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.STP_RESULTS_EMAIL_NOTIFICATION.name())
                .templateReference(notificationPropertiesDetails.getTemplateReference())
                .recipientAddress(notificationPropertiesDetails.getRecipientAddress())
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(mapStpResultParameter(emailNotificationInput.getBankGuaranteeRequest()))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(NotificationHelper.getCCAddress(notificationPropertiesDetails.getCcAddresses(), environment))
                .locale(US.toLanguageTag())
                .build();
    }

    private Map<String, String> mapStpResultParameter(BankGuaranteeRequest bankGuaranteeRequest) {
        BankGuaranteeRequestData bankGuaranteeRequestData = bankGuaranteeRequest.getBgRequest();
        InstructingPartyData.OrganisationData organisation = bankGuaranteeRequestData.getInstructingParty().getOrganisation();
        DossierData dossierInformation = bankGuaranteeRequestData.getDossierInformation();
        Map<String, String> stpResultParameter = new HashMap<>();
        stpResultParameter.put(REFERENCE_NUMBER, bankGuaranteeRequestData.getReferenceNumber());
        String agreementRequestName = String.format("%s-%s", AGREEMENT_DOSSIER_TYPE_CODE, dossierInformation.getAgreementDossierResponseId());
        stpResultParameter.put(DOSSIER_URL, dossierUrl);
        stpResultParameter.put(PEGA_ID, bankGuaranteeRequest.getPegaCaseId());
        stpResultParameter.put(AGREEMENT_REQUEST_NAME, agreementRequestName);
        stpResultParameter.put(INSTRUCTING_PARTY_ID, organisation.getLegalEntityId());
        stpResultParameter.put(MASTER_REFERENCE_NUMBER, bankGuaranteeRequest.getMasterReferenceNumber());
        stpResultParameter.put(FAILURE_REMARKS, bankGuaranteeRequestData.getFailureRemarks());
        stpResultParameter.put(CSI_BE, getCsiIdentifierValue(organisation.getInternalIdentifiers()));
        NotificationHelper.prepareStpResultSet(stpResultParameter, bankGuaranteeRequest);
        return NotificationHelper.sanitizeParams(stpResultParameter);
    }
    private String getCsiIdentifierValue(List<Identifier> internalIdentifiers) {
        return getIdentifierValue(internalIdentifiers, List.of(ConstantUtils.CSI,ConstantUtils.CSI_BE_SOLE_PRPTRP_ID))
                .map(Identifier::getValue)
                .orElse(null);
    }
}
