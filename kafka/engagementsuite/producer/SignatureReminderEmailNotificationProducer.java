package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
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
import static com.ing.bankguarantees.utils.CommonUtils.getEmailDigitalAddress;
import static com.ing.bankguarantees.utils.ConstantUtils.*;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignatureReminderEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing beneficiary notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getSignatureReminderEmailNotificationProperties().getTopic(),
                createNotificationCallback(notificationEvent, notificationProperties.getSignatureReminderEmailNotificationProperties()));
    }


    private EmailNotificationCallback createNotificationCallback(NotificationEvent notificationEvent,
                                                                 NotificationPropertiesDetails requesterSignCompletionEmailProperties) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(requesterSignCompletionEmailProperties)
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
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        metadata.put(RECEIVER_LR_NAME, bankGuaranteeRequestData.getInstructingParty().getIndividual().getIndividualName().getFullName());
        return Header.builder()
                .id(java.util.UUID.randomUUID().toString())
                .traceId(emailNotificationInput.getTraceId())
                .created(Instant.now().toEpochMilli())
                .source(getSource())
                .metadata(metadata)
                .build();
    }


    private NotificationEventBody createBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getSignatureReminderEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.SIGNATURE_REMINDER_EMAIL_NOTIFICATION.name())
                .templateReference(getTemplateReference(notificationPropertiesDetails, emailNotificationInput))
                .recipientAddress(getRecipient(emailNotificationInput))
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(prepareBodyParam(emailNotificationInput))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(notificationPropertiesDetails.getCcAddresses())
                .locale(US.toLanguageTag())
                .build();
    }


    private Map<String, String> prepareBodyParam(EmailNotificationInput emailNotificationInput) {
        var bgRequest = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        return Map.of(LEGAL_REP_NAME, emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getInstructingParty().getIndividual().getIndividualName().getFullName(),
                REFERENCE_NUMBER, bgRequest.getReferenceNumber(),
                SIGN_EXPIRY_DATE, NotificationHelper.formatDate(bgRequest.getSignExpiryDate(), DATE_TIME_FORMATTER_DD_MMMM_YYYY));
    }


    private String getTemplateReference(NotificationPropertiesDetails notificationPropertiesDetails, EmailNotificationInput emailNotificationInput) {
        String language = emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getTranslationLanguage().getLanguage();
        return String.format("%s-%s", notificationPropertiesDetails.getTemplateReference(), language);
    }


    private String getRecipient(EmailNotificationInput emailNotificationInput) {
        return Strings.CS.equals(environment, ENV_PRD)
                ? getEmailDigitalAddress(emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getInstructingParty().getIndividual().getDigitalAddresses())
                : DEFAULT_EMAIL;
    }



}