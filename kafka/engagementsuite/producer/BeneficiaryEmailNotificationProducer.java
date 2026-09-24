package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.*;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback.EmailNotificationCallback;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper;
import com.ing.bankguarantees.utils.CommonUtils;
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
import static com.ing.bankguarantees.utils.ConstantUtils.DEFAULT_EMAIL;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static java.util.Locale.US;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryEmailNotificationProducer {

    private final NotificationProperties notificationProperties;
    private final EngagementSuiteGateway engagementSuiteGateway;

    @Value("${bgos.app.env}")
    private String environment;

    public void notify(EmailNotificationInput emailNotificationInput) {
        log.info("start preparing beneficiary notifications for request {}", emailNotificationInput.getBankGuaranteeRequest().getRequestId());
        NotificationEvent notificationEvent = createNotificationEvent(emailNotificationInput);
        engagementSuiteGateway.send(notificationEvent, notificationProperties.getBeneficiaryEmailNotificationProperties().getTopic(),
                createNonStpNotificationCallback(notificationEvent, notificationProperties.getBeneficiaryEmailNotificationProperties()));
    }


    private EmailNotificationCallback createNonStpNotificationCallback(NotificationEvent notificationEvent,
                                                                       NotificationPropertiesDetails nonStpEmailNotificationProperties) {
        return EmailNotificationCallback.builder()
                .notificationEvent(notificationEvent)
                .notificationPropertiesDetails(nonStpEmailNotificationProperties)
                .build();
    }

    private NotificationEvent createNotificationEvent(EmailNotificationInput emailNotificationInput) {
        log.info("build notification event request for request id {}", emailNotificationInput.getBankGuaranteeRequest().getBgRequest());
        return NotificationEvent.builder()
                .header(createHeader(emailNotificationInput))
                .body(createBody(emailNotificationInput))
                .attachments(createAttachmentBody(emailNotificationInput))
                .build();
    }


    private Header createHeader(EmailNotificationInput emailNotificationInput) {
        log.info("StpBeneficiaryEmailNotificationProducer [createHeader] call");
        return Header.builder()
                .id(java.util.UUID.randomUUID().toString())
                .traceId(emailNotificationInput.getTraceId())
                .created(Instant.now().toEpochMilli())
                .source(getSource())
                .metadata(NotificationHelper.prepareMetadataParam(emailNotificationInput))
                .build();
    }


    private NotificationEventBody createBody(EmailNotificationInput emailNotificationInput) {
        log.info("StpBeneficiaryEmailNotificationProducer [createBody] call");
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getBeneficiaryEmailNotificationProperties();

        return NotificationEventBody.builder()
                .profileId(emailNotificationInput.getProfileId())
                .communication(SupportedCommunication.BENEFICIARY_EMAIL_NOTIFICATION.name())
                .templateReference(getTemplateReference(notificationPropertiesDetails, emailNotificationInput))
                .recipientAddress(getRecipient(emailNotificationInput))
                .senderAddress(notificationPropertiesDetails.getSenderAddress())
                .templateParams(prepareBodyParam(emailNotificationInput))
                .channel(notificationPropertiesDetails.getChannel())
                .ccAddresses(getBeneficiaryCCAddress(notificationPropertiesDetails, emailNotificationInput))
                .locale(US.toLanguageTag())
                .build();
    }

    private List<String> getBeneficiaryCCAddress(NotificationPropertiesDetails notificationPropertiesDetails, EmailNotificationInput emailNotificationInput) {
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        if (Strings.CS.equals(environment, ENV_PRD))
            return List.of(CommonUtils.getEmailDigitalAddress(bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses()));
        return notificationPropertiesDetails.getCcAddresses();
    }

    private String getRecipient(EmailNotificationInput emailNotificationInput) {
        return Strings.CS.equals(environment, ENV_PRD)
                ? emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getBeneficiary().getEmailAddress().getEmailIdInformation()
                : DEFAULT_EMAIL;
    }


    private Map<String, String> prepareBodyParam(EmailNotificationInput emailNotificationInput) {
        var bgRequest = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        return Map.of(REFERENCE_NUMBER, bgRequest.getReferenceNumber(),
                MASTER_REFERENCE_NUMBER, emailNotificationInput.getBankGuaranteeRequest().getMasterReferenceNumber(),
                BENEFICIARY_NAME, bgRequest.getBeneficiary().getOrganisationName().getFullName(),
                INSTRUCTING_PARTY_NAME, bgRequest.getInstructingParty().getOrganisation().getOrganisationName().getFullName());
    }


    private String getTemplateReference(NotificationPropertiesDetails notificationPropertiesDetails, EmailNotificationInput emailNotificationInput) {
        var guaranteeDetails = emailNotificationInput.getBankGuaranteeRequest().getBgRequest().getGuaranteeDetails();
        return String.format("%s-%s", notificationPropertiesDetails.getTemplateReference(), guaranteeDetails.getBgLanguage().getLanguageCode());
    }


    private @Valid @NotNull List<NotificationAttachment> createAttachmentBody(EmailNotificationInput emailNotificationInput) {
        log.info("StpBeneficiaryEmailNotificationProducer [createAttachmentBody] call");
        NotificationPropertiesDetails notificationPropertiesDetails = notificationProperties.getBeneficiaryEmailNotificationProperties();
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