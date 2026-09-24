package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;
import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.getSource;
import static com.ing.bankguarantees.utils.CommonUtils.getEmailDigitalAddress;
import static com.ing.bankguarantees.utils.ConstantUtils.DEFAULT_EMAIL;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing customer notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getCustomerEmailNotificationProperties().getTopic(),
                createNotificationCallback(notificationEvent, notificationProperties.getCustomerEmailNotificationProperties()));
    }


    private EmailNotificationCallback createNotificationCallback(NotificationEvent notificationEvent,
                                                                 NotificationPropertiesDetails nonStpEmailNotificationProperties) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(nonStpEmailNotificationProperties)
                .build();
    }

    private NotificationEvent createNotificationEvent(EmailNotificationInput emailNotificationInput) {
        return NotificationEvent.builder()
                .header(createHeader(emailNotificationInput))
                .body(createBody(emailNotificationInput))
                .attachments(createAttachmentBody(emailNotificationInput))
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
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getCustomerEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.CUSTOMER_EMAIL_NOTIFICATION.name())
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
                MASTER_REFERENCE_NUMBER, emailNotificationInput.getBankGuaranteeRequest().getMasterReferenceNumber(),
                LEGAL_REP_NAME, bgRequest.getInstructingParty().getIndividual().getIndividualName().getFullName());
    }

    private String getTemplateReference(NotificationPropertiesDetails notificationPropertiesDetails,
                                        EmailNotificationInput emailNotificationInput) {
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        return String.format("%s-%s", notificationPropertiesDetails.getTemplateReference(), bankGuaranteeRequestData.getTranslationLanguage().getLanguage());
    }

    private @Valid @NotNull List<NotificationAttachment> createAttachmentBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getCustomerEmailNotificationProperties();
        return emailNotificationInput.getDocumentList()
                .stream()
                .map(Document::getDocumentId)
                .filter(StringUtils::isNotEmpty)
                .map(documentId -> NotificationAttachment.builder()
                        .processorType(AttachmentProcessorType.API_ENDPOINT_DISCOVERY)
                        .aedEndpoint(notificationPropertiesDetails.getAedEndpoint())
                        .attachmentUrl(String.format(notificationPropertiesDetails.getAttachmentUrl(), documentId))
                        .build())
                .toList();
    }

}
