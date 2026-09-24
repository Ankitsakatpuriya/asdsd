package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignatureReminderEmailNotificationProducerTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    public static final String APPLICATION_ID = "applicationId";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private EngagementSuiteGateway engagementSuiteGateway;

    private NotificationPropertiesDetails signatureReminderPropertiesDetails;
    private SignatureReminderEmailNotificationProducer signatureReminderEmailNotificationProducer;

    @BeforeEach
    void setup() {
        signatureReminderPropertiesDetails = MockHelper.getSignatureReminderPropertiesDetails();
        NotificationProperties notificationProperties = new NotificationProperties();
        notificationProperties.setSignatureReminderEmailNotificationProperties(signatureReminderPropertiesDetails);
        signatureReminderEmailNotificationProducer = new SignatureReminderEmailNotificationProducer(notificationProperties, engagementSuiteGateway);
        ReflectionTestUtils.setField(signatureReminderEmailNotificationProducer, "environment", "PRD");
    }

    @Test
    void notifyPositive() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        EmailNotificationInput nonStpEmailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, Collections.emptyList());
        doNothing().when(engagementSuiteGateway).send(any(), any(), any());
        ArgumentCaptor<NotificationEvent> argumentCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

        signatureReminderEmailNotificationProducer.notify(nonStpEmailNotificationInput);
        String expectedTemplateName = signatureReminderPropertiesDetails.getTemplateReference() + "-" + bankGuaranteeRequestData.getTranslationLanguage().getLanguage();
        verify(engagementSuiteGateway, times(1)).send(argumentCaptor.capture(), any(), any());
        NotificationEvent notificationRequest = argumentCaptor.getValue();
        assertThat(notificationRequest).isNotNull();
        assertThat(notificationRequest.getBody()).isNotNull();
        assertThat(notificationRequest.getHeader()).isNotNull();
        assertThat(notificationRequest.getAttachments()).isNotNull();
        assertThat(notificationRequest.getHeader().getTraceId()).isEqualTo(nonStpEmailNotificationInput.getTraceId());
        assertThat(notificationRequest.getHeader().getMetadata().get(APPLICATION_ID)).isEqualTo(bankGuaranteeRequestData.getRequestId());
        assertThat(notificationRequest.getBody().getProfileId()).isEqualTo(bankGuaranteeRequest.getIndividualId());
        assertThat(notificationRequest.getBody().getChannel()).isEqualTo(signatureReminderPropertiesDetails.getChannel());
        assertThat(notificationRequest.getBody().getSenderAddress()).isEqualTo(signatureReminderPropertiesDetails.getSenderAddress());
        assertThat(notificationRequest.getBody().getRecipientAddress()).isEqualTo(CommonUtils.getEmailDigitalAddress(bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses()));
        assertThat(notificationRequest.getBody().getTemplateReference()).isEqualTo(expectedTemplateName);
        assertThat(notificationRequest.getBody().getCommunication()).isEqualTo(SupportedCommunication.SIGNATURE_REMINDER_EMAIL_NOTIFICATION.name());
        assertThat(notificationRequest.getBody().getTemplateParams()).isNotEmpty();
        assertThat(notificationRequest.getAttachments()).isEmpty();
    }

    @Test
    void notifyError() {

        try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());
            BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
            BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
            StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
            bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
            bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
            EmailNotificationInput nonStpEmailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, Collections.emptyList());
            BgosException exception = assertThrows(BgosException.class, () -> signatureReminderEmailNotificationProducer.notify(nonStpEmailNotificationInput));
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
        }
    }
}
