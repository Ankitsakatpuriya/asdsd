package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.mapper.NonStpEmailParamMapper;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.getSource;
import static com.ing.bankguarantees.utils.ConstantUtils.DEFAULT_EMAIL;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonStpEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;
    private final NonStpEmailParamMapper nonStpEmailParamMapper;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing non stp notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getNonStpEmailNotificationProperties().getTopic(),
                createNotificationCallback(notificationEvent, notificationProperties.getNonStpEmailNotificationProperties()));
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
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getNonStpEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.NON_STP_EMAIL_NOTIFICATION.name())
                .templateReference(notificationPropertiesDetails.getTemplateReference())
                .recipientAddress(getRecipient(notificationPropertiesDetails))
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(nonStpEmailParamMapper.prepareBodyParam(emailNotificationInput))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(NotificationHelper.getCCAddress(notificationPropertiesDetails.getCcAddresses(), environment))
                .locale(US.toLanguageTag())
                .build();
    }

    private String getRecipient(NotificationPropertiesDetails notificationPropertiesDetails) {
        return Strings.CS.equals(environment, ENV_PRD)
                ? notificationPropertiesDetails.getRecipientAddress()
                : DEFAULT_EMAIL;
    }

    private @Valid @NotNull List<NotificationAttachment> createAttachmentBody(EmailNotificationInput emailNotificationInput) {
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getNonStpEmailNotificationProperties();
        return List.of(NotificationAttachment.builder()
                .processorType(AttachmentProcessorType.TEMPLATE_MANAGER)
                .fileContentType(notificationPropertiesDetails.getAttachmentContentType())
                .fileName(getFileName(emailNotificationInput.getBankGuaranteeRequest(), notificationPropertiesDetails.getAttachmentFileName()))
                .templateLocale(US.toLanguageTag())
                .templateName(notificationPropertiesDetails.getAttachmentTemplateReference())
                .templateParams(nonStpEmailParamMapper.prepareAttachmentParameter(emailNotificationInput))
                .build());
    }


    private String getFileName(BankGuaranteeRequest bankGuaranteeRequest, String fileName) {
        return String.format(fileName,
                bankGuaranteeRequest.getBgRequest().getReferenceNumber(),
                bankGuaranteeRequest.getBgRequest().getGuaranteeDetails().getBgCode().toString().toLowerCase()
        );
    }

}
