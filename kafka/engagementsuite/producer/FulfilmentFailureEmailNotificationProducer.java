package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.BENEFICIARY_NAME;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.REFERENCE_NUMBER;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.getSource;
import static com.ing.bankguarantees.utils.CommonUtils.getEmailDigitalAddress;
import static com.ing.bankguarantees.utils.ConstantUtils.DEFAULT_EMAIL;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfilmentFailureEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing fulfilment failure notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getFulfilmentFailureEmailNotificationProperties().getTopic(),
                createNotificationCallback(notificationEvent, notificationProperties.getFulfilmentFailureEmailNotificationProperties()));
    }


    private EmailNotificationCallback createNotificationCallback(NotificationEvent notificationEvent, NotificationPropertiesDetails fulfilmentFailureEmailNotificationProperties) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(fulfilmentFailureEmailNotificationProperties)
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
                .id(UUID.randomUUID().toString())
                .traceId(emailNotificationInput.getTraceId())
                .created(Instant.now().toEpochMilli())
                .source(getSource())
                .metadata(NotificationHelper.prepareMetadataParam(emailNotificationInput))
                .build();
    }


    private NotificationEventBody createBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getFulfilmentFailureEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.FULFILMENT_FAILURE_EMAIL_NOTIFICATION.name())
                .templateReference(getTemplateReference(notificationPropertiesDetails, emailNotificationInput))
                .recipientAddress(getRecipient(emailNotificationInput))
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(prepareBodyParam(emailNotificationInput))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(NotificationHelper.getCCAddress(notificationPropertiesDetails.getCcAddresses(), environment))
                .locale(US.toLanguageTag())
                .build();
    }

    private String getRecipient(EmailNotificationInput emailNotificationInput) {
        return Strings.CS.equals(environment, ENV_PRD)
                ? getEmailDigitalAddress(emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getInstructingParty().getIndividual().getDigitalAddresses())
                : DEFAULT_EMAIL;
    }

    private Map<String, String> prepareBodyParam(EmailNotificationInput emailNotificationInput) {
        var bgRequest = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        return Map.of(REFERENCE_NUMBER, bgRequest.getReferenceNumber(),
                BENEFICIARY_NAME, bgRequest.getInstructingParty().getIndividual().getIndividualName().getFullName());
    }

    private String getTemplateReference(NotificationPropertiesDetails notificationPropertiesDetails,
                                        EmailNotificationInput emailNotificationInput) {
        Locale translationLanguage = emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getTranslationLanguage();
        return String.format("%s-%s", notificationPropertiesDetails.getTemplateReference(), translationLanguage.getLanguage());
    }

}
