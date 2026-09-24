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
public class RequesterPartiallySignedEmailNotificationProducerTest {

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

    private NotificationPropertiesDetails requesterPartiallySignedPropertiesDetails;
    private RequesterPartiallySignedEmailNotificationProducer requesterPartiallySignedEmailNotificationProducer;

    @BeforeEach
    void setup() {
        requesterPartiallySignedPropertiesDetails = MockHelper.getRequesterPartiallySignedPropertiesDetails();
        NotificationProperties notificationProperties = new NotificationProperties();
        notificationProperties.setRequesterPartiallySignedEmailNotificationProperties(requesterPartiallySignedPropertiesDetails);
        requesterPartiallySignedEmailNotificationProducer = new RequesterPartiallySignedEmailNotificationProducer(notificationProperties, engagementSuiteGateway);
        ReflectionTestUtils.setField(requesterPartiallySignedEmailNotificationProducer, "environment", "PRD");
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

        requesterPartiallySignedEmailNotificationProducer.notify(nonStpEmailNotificationInput);
        String expectedTemplateName = requesterPartiallySignedPropertiesDetails.getTemplateReference() + "-" + bankGuaranteeRequestData.getTranslationLanguage().getLanguage();
        verify(engagementSuiteGateway, times(1)).send(argumentCaptor.capture(), any(), any());
        NotificationEvent notificationRequest = argumentCaptor.getValue();
        assertThat(notificationRequest).isNotNull();
        assertThat(notificationRequest.getBody()).isNotNull();
        assertThat(notificationRequest.getHeader()).isNotNull();
        assertThat(notificationRequest.getAttachments()).isNotNull();
        assertThat(notificationRequest.getHeader().getTraceId()).isEqualTo(nonStpEmailNotificationInput.getTraceId());
        assertThat(notificationRequest.getHeader().getMetadata().get(APPLICATION_ID)).isEqualTo(bankGuaranteeRequestData.getRequestId());
        assertThat(notificationRequest.getBody().getProfileId()).isEqualTo(bankGuaranteeRequest.getIndividualId());
        assertThat(notificationRequest.getBody().getChannel()).isEqualTo(requesterPartiallySignedPropertiesDetails.getChannel());
        assertThat(notificationRequest.getBody().getSenderAddress()).isEqualTo(requesterPartiallySignedPropertiesDetails.getSenderAddress());
        assertThat(notificationRequest.getBody().getRecipientAddress()).isEqualTo(CommonUtils.getEmailDigitalAddress(bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses()));
        assertThat(notificationRequest.getBody().getTemplateReference()).isEqualTo(expectedTemplateName);
        assertThat(notificationRequest.getBody().getCommunication()).isEqualTo(SupportedCommunication.REQUESTER_PARTIALLY_SIGN_EMAIL_NOTIFICATION.name());
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
            BgosException exception = assertThrows(BgosException.class, () -> requesterPartiallySignedEmailNotificationProducer.notify(nonStpEmailNotificationInput));
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
        }
    }
}
