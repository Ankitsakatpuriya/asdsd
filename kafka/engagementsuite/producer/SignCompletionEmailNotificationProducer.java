package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.getSource;
import static com.ing.bankguarantees.utils.ConstantUtils.DEFAULT_EMAIL;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignCompletionEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing beneficiary notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getSignCompletionEmailNotificationProperties().getTopic(),
                createSignCompletionNotificationCallback(notificationEvent, notificationProperties.getSignCompletionEmailNotificationProperties()));
    }


    private EmailNotificationCallback createSignCompletionNotificationCallback(NotificationEvent notificationEvent,
                                                                               NotificationPropertiesDetails signCompletionEmailProperties) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(signCompletionEmailProperties)
                .build();
    }

    private NotificationEvent createNotificationEvent(EmailNotificationInput emailNotificationInput) {
        log.info("build notification event request for request id {}", emailNotificationInput.getBankGuaranteeRequest().getBgRequest());
        return NotificationEvent.builder()
                .header(createHeader(emailNotificationInput))
                .body(createBody(emailNotificationInput))
                .attachments(Collections.emptyList())
                .build();
    }


    private Header createHeader(EmailNotificationInput emailNotificationInput) {
        Map<String, String> metadata = NotificationHelper.prepareMetadataParam(emailNotificationInput);
        metadata.put(RECEIVER_LR_NAME, emailNotificationInput.getReceiverLegalRep().getFullName());
        return Header.builder()
                .id(java.util.UUID.randomUUID().toString())
                .traceId(emailNotificationInput.getTraceId())
                .created(Instant.now().toEpochMilli())
                .source(getSource())
                .metadata(metadata)
                .build();
    }


    private NotificationEventBody createBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getSignCompletionEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.SIGN_COMPLETION_EMAIL_NOTIFICATION.name())
                .templateReference(getTemplateReference(notificationPropertiesDetails, emailNotificationInput))
                .recipientAddress(getRecipient(emailNotificationInput))
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(prepareBodyParam(emailNotificationInput))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(NotificationHelper.getCCAddress(notificationPropertiesDetails.getCcAddresses(), environment))
                .locale(US.toLanguageTag())
                .build();
    }


    private Map<String, String> prepareBodyParam(EmailNotificationInput emailNotificationInput) {
        var bgRequest = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        return Map.of(RECEIVER_LR_NAME, emailNotificationInput.getReceiverLegalRep().getFullName(),
                LEGAL_REP_NAME, emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getInstructingParty().getIndividual().getIndividualName().getFullName(),
                INSTRUCTING_PARTY_NAME, bgRequest.getInstructingParty().getOrganisation().getOrganisationName().getFullName(),
                REFERENCE_NUMBER, bgRequest.getReferenceNumber());
    }


    private String getTemplateReference(NotificationPropertiesDetails notificationPropertiesDetails, EmailNotificationInput emailNotificationInput) {
        LegalRepresentativeData receiverLegalRep = emailNotificationInput.getReceiverLegalRep();
        return String.format("%s-%s", notificationPropertiesDetails.getTemplateReference(), getLrPreferredLanguage(receiverLegalRep.getPreferredLanguage()));
    }

    private String getLrPreferredLanguage(String preferredLanguage) {
        return switch (preferredLanguage) {
            case "fr", "en" -> preferredLanguage;
            default -> "nl";
        };
    }


    private String getRecipient(EmailNotificationInput emailNotificationInput) {
        return Strings.CS.equals(environment, ENV_PRD)
                ? emailNotificationInput.getReceiverLegalRep().getEmailId()
                : DEFAULT_EMAIL;
    }


}