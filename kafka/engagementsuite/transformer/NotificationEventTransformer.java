package com.ing.bankguarantees.remote.kafka.engagementsuite.transformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.AttachmentProcessorType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationAttachment;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEventBody;
import com.ing.tpa.esuite.notificationapi.domain.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.COMMUNICATION;
import static java.util.stream.Collectors.joining;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventTransformer {

    private static final String DISTRIBUTION_CHANNEL_ID = "P31464/DistributionChannel/BankGuaranteeApplicationEmail";
    private static final ObjectMapper defaultMapper = createDefaultMapper();
    private static final String CSO_BE = "CSO_BE";
    private static final String EVENT_ID_PREFIX = "BGOS-email-";

    private final Validator validator;

    private static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class DistributionChannelParameters {
        List<String> ccAddress;
        List<String> bccAddress;
        List<String> attachmentsUrl;
    }

    public EngagementSuiteNotificationEvent transform(NotificationEvent request) throws JacksonException {

        validate(request);

        NotificationTarget notificationTarget = NotificationTarget.newBuilder()
                .setRecipientAddress(request.getBody().getRecipientAddress())
                .setProfileId(request.getBody().getProfileId())
                .build();

        NotificationMessage notificationMessage = NotificationMessage.newBuilder()
                .setTemplateIdentifier(request.getBody().getTemplateReference())
                .setTemplateParameters(transformTemplateParameters(request.getBody().getTemplateParams()).orElse(null))
                .setTemplateLocale(request.getBody().getLocale())
                .setAttachments(getAttachmentList(request))
                .build();

        Optional<String> transformedChannelParams = transformChannelParameters(request.getBody());
        NotificationDistributionChannel notificationDistributionChannel = NotificationDistributionChannel.newBuilder()
                .setIdentifier(DISTRIBUTION_CHANNEL_ID)
                .setCso(CSO_BE)
                .setParameters(transformedChannelParams.orElse(null))
                .build();

        return EngagementSuiteNotificationEvent.newBuilder()
                .setNotificationTarget(notificationTarget)
                .setNotificationMessage(notificationMessage)
                .setNotificationDistributionChannel(notificationDistributionChannel)
                .setNotificationMetadata(transformMetadata(request.getHeader().getMetadata(), request.getBody()))
                .setId(EVENT_ID_PREFIX + request.getHeader().getId())
                .build();
    }

    private Attachments getAttachmentList(NotificationEvent notificationEvent) throws JacksonException {

        return Attachments.newBuilder()
                .setApiEndpointDiscovery(getApiEndpointDiscoveryList(notificationEvent.getAttachments()))
                .setTemplateManagerApi(getTemplateManagerAttachmentList(notificationEvent.getAttachments()))
                .build();
    }

    private List<TemplateManagerApi> getTemplateManagerAttachmentList(List<NotificationAttachment> notificationAttachmentList) throws JacksonException {
        var templateManagerApiList = new ArrayList<TemplateManagerApi>();

        for (NotificationAttachment notificationAttachment : notificationAttachmentList) {
            if (notificationAttachment.getProcessorType() == AttachmentProcessorType.TEMPLATE_MANAGER)
                templateManagerApiList.add(TemplateManagerApi.newBuilder()
                        .setFileName(notificationAttachment.getFileName())
                        .setTemplateName(notificationAttachment.getTemplateName())
                        .setTemplateParameters(transformTemplateParameters(notificationAttachment.getTemplateParams()).orElse(null))
                        .setFileContentType(notificationAttachment.getFileContentType())
                        .setTemplateLocale(notificationAttachment.getTemplateLocale())
                        .build());
        }
        return templateManagerApiList;
    }

    private List<ApiEndpointDiscovery> getApiEndpointDiscoveryList(List<NotificationAttachment> notificationAttachmentList) {
        var apiEndpointDiscoveryList = new ArrayList<ApiEndpointDiscovery>();

        for (NotificationAttachment notificationAttachment : notificationAttachmentList) {
            if (notificationAttachment.getProcessorType() == AttachmentProcessorType.API_ENDPOINT_DISCOVERY)
                apiEndpointDiscoveryList.add(ApiEndpointDiscovery.newBuilder()
                        .setAedEndpoint(notificationAttachment.getAedEndpoint())
                        .setAttachmentUrl(notificationAttachment.getAttachmentUrl())
                        .build());
        }
        return apiEndpointDiscoveryList;
    }

    private Map<String, String> transformMetadata(Map<String, String> metadata, NotificationEventBody body) {

        if (MapUtils.isEmpty(metadata)) {
            metadata = new HashMap<>();
        }
        metadata.put(COMMUNICATION, body.getCommunication());
        return metadata;
    }


    private Optional<String> transformTemplateParameters(Map<String, String> templateParams) throws JacksonException {
        if (MapUtils.isEmpty(templateParams)) {
            return Optional.empty();
        }
        return Optional.ofNullable(defaultMapper.writeValueAsString(templateParams));
    }


    private Optional<String> transformChannelParameters(NotificationEventBody notificationEventBody) throws JacksonException {

        DistributionChannelParameters distributionChannelParameters = new DistributionChannelParameters();

        if (CollectionUtils.isNotEmpty(notificationEventBody.getCcAddresses())) {
            distributionChannelParameters.setCcAddress(notificationEventBody.getCcAddresses());
        }
        return Optional.ofNullable(defaultMapper.writeValueAsString(distributionChannelParameters));
    }


    private void validate(NotificationEvent notificationEvent) {
        Set<ConstraintViolation<Object>> violations = validator.validate(notificationEvent);
        if (!violations.isEmpty()) {
            log.error("NotificationEvent  found with the {}  violation(s)  --> {}  ",
                    violations.size(),
                    violations.stream().map(objectConstraintViolation -> objectConstraintViolation.getPropertyPath() + " "
                            + objectConstraintViolation.getMessage()).collect(joining(" :: ")));
            throw new BgosException(ErrorCode.TECHNICAL_ERROR);
        }
    }
}
