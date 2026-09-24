
package com.ing.bankguarantees.remote.kafka.engagementsuite.transformer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.AttachmentProcessorType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationAttachment;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.tpa.esuite.notificationapi.domain.ApiEndpointDiscovery;
import com.ing.tpa.esuite.notificationapi.domain.EngagementSuiteNotificationEvent;
import com.ing.tpa.esuite.notificationapi.domain.TemplateManagerApi;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class NotificationEventTransformerTest {

    private static final String CSO_BE = "CSO_BE";
    private static final String NON_STP_HTML_TEMPLATE = "P31464/Template/non-stp-html-document";
    private static final String NON_STP_XML_TEMPLATE = "P31464/Template/non-stp-xml-document";
    private static final String NOTIFICATION_EVENT = "BGA/BGF/notification_request.json";
    private NotificationEventTransformer notificationEventTransformer;

    private Validator validator;

    @BeforeEach
    void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        notificationEventTransformer = new NotificationEventTransformer(validator);
    }

    @Test
    @SneakyThrows
    void transformForTemplateManagerSuccess() {
        NotificationEvent notificationEvent = MockHelper.creteNotificationEvent(NOTIFICATION_EVENT);
        EngagementSuiteNotificationEvent engagementSuiteNotificationEvent = notificationEventTransformer.transform(notificationEvent);
        assertEquals(NON_STP_HTML_TEMPLATE, engagementSuiteNotificationEvent.getNotificationMessage().getTemplateIdentifier());
        String templateParams = engagementSuiteNotificationEvent.getNotificationMessage().getTemplateParameters();
        TemplateManagerApi templateManagerApi = engagementSuiteNotificationEvent.getNotificationMessage().getAttachments().getTemplateManagerApi().get(0);
        assertTrue(templateParams.contains("\"legalRepName\":\"NESTORINETD BEN ALAMITD\""));
        String attachmentParameters = templateManagerApi.getTemplateParameters();
        assertTrue(attachmentParameters.contains("\"beneficieryName\":\"LA LOTERIE NATIONALE\""));
        assertEquals(CSO_BE, engagementSuiteNotificationEvent.getNotificationDistributionChannel().getCso());
        assertEquals(NON_STP_XML_TEMPLATE, templateManagerApi.getTemplateName());
    }

    @Test
    @SneakyThrows
    void transformForApiEndpointDiscoverySuccess() {
        NotificationEvent notificationEvent = MockHelper.creteNotificationEvent(NOTIFICATION_EVENT);
        NotificationAttachment notificationAttachment = notificationEvent.getAttachments().get(0);
        notificationAttachment.setProcessorType(AttachmentProcessorType.API_ENDPOINT_DISCOVERY);
        EngagementSuiteNotificationEvent engagementSuiteNotificationEvent = notificationEventTransformer.transform(notificationEvent);
        assertEquals(NON_STP_HTML_TEMPLATE, engagementSuiteNotificationEvent.getNotificationMessage().getTemplateIdentifier());
        String templateParams = engagementSuiteNotificationEvent.getNotificationMessage().getTemplateParameters();
        ApiEndpointDiscovery apiEndpointDiscovery = engagementSuiteNotificationEvent.getNotificationMessage().getAttachments().getApiEndpointDiscovery().get(0);
        assertTrue(templateParams.contains("\"legalRepName\":\"NESTORINETD BEN ALAMITD\""));
        assertEquals(CSO_BE, engagementSuiteNotificationEvent.getNotificationDistributionChannel().getCso());
        assertEquals(apiEndpointDiscovery.getAedEndpoint(), notificationEvent.getAttachments().get(0).getAedEndpoint());
        assertEquals(apiEndpointDiscovery.getAttachmentUrl(), notificationEvent.getAttachments().get(0).getAttachmentUrl());
    }

    @Test
    @SneakyThrows
    void transformFailure() {
        NotificationEvent notificationEvent = MockHelper.creteNotificationEvent(NOTIFICATION_EVENT);
        notificationEvent.setBody(null);
        BgosException exception = assertThrows(BgosException.class, () -> notificationEventTransformer.transform(notificationEvent));
        assertEquals(ErrorCode.TECHNICAL_ERROR, exception.getErrorCode());

    }


}
