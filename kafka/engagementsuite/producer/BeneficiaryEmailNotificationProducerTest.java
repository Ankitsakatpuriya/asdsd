package com.ing.bankguarantees.remote.kafka.engagementsuite.producer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.EngagementSuiteGateway;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationProperties;
import com.ing.bankguarantees.remote.kafka.engagementsuite.NotificationPropertiesDetails;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.NotificationEvent;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeneficiaryEmailNotificationProducerTest {

    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String APPLICATION_ID = "applicationId";
    private static final String DOCUMENT_ID = "344135tplow";
    private static final String PRD_ENV = "PRD";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";

    @Mock
    private EngagementSuiteGateway engagementSuiteGateway;

    private NotificationPropertiesDetails nonStpProperties;
    private BeneficiaryEmailNotificationProducer beneficiaryEmailNotificationProducer;

    @BeforeEach
    void setup() {
        nonStpProperties = MockHelper.getBeneficiaryPropertyDetail();
        nonStpProperties.setAedEndpoint("aedEndpoint");
        nonStpProperties.setAttachmentUrl("attachementurl");
        NotificationProperties notificationProperties = new NotificationProperties();
        notificationProperties.setBeneficiaryEmailNotificationProperties(nonStpProperties);
        beneficiaryEmailNotificationProducer = new BeneficiaryEmailNotificationProducer(notificationProperties, engagementSuiteGateway);
        ReflectionTestUtils.setField(beneficiaryEmailNotificationProducer, "environment", PRD_ENV);
    }

    @Test
    void notifyPositive() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setMasterReferenceNumber(MASTER_REFERENCE_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        document.setDocumentType(DocumentType.BG_FINAL);
        document.setStatus(DocumentStatus.READY);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        EmailNotificationInput nonStpEmailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        doNothing().when(engagementSuiteGateway).send(any(), any(), any());
        ArgumentCaptor<NotificationEvent> argumentCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

        beneficiaryEmailNotificationProducer.notify(nonStpEmailNotificationInput);

        verify(engagementSuiteGateway, times(1)).send(argumentCaptor.capture(), any(), any());
        NotificationEvent notificationRequest = argumentCaptor.getValue();
        assertThat(notificationRequest).isNotNull();
        assertThat(notificationRequest.getBody()).isNotNull();
        assertThat(notificationRequest.getHeader()).isNotNull();
        assertThat(notificationRequest.getAttachments()).isNotNull();
        assertThat(notificationRequest.getHeader().getTraceId()).isEqualTo(nonStpEmailNotificationInput.getTraceId());
        assertThat(notificationRequest.getHeader().getMetadata().get(APPLICATION_ID)).isEqualTo(bankGuaranteeRequestData.getRequestId());
        assertThat(notificationRequest.getBody().getProfileId()).isEqualTo(bankGuaranteeRequest.getIndividualId());
        assertThat(notificationRequest.getBody().getChannel()).isEqualTo(nonStpProperties.getChannel());
        assertThat(notificationRequest.getBody().getSenderAddress()).isEqualTo(nonStpProperties.getSenderAddress());
        assertThat(notificationRequest.getBody().getRecipientAddress()).isEqualTo(bankGuaranteeRequestData.getBeneficiary().getEmailAddress().getEmailIdInformation());
        String expectedTemplateName = nonStpProperties.getTemplateReference() + "-" + bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode();
        assertThat(notificationRequest.getBody().getTemplateReference()).isEqualTo(expectedTemplateName);
        assertThat(notificationRequest.getBody().getCommunication()).isEqualTo(SupportedCommunication.BENEFICIARY_EMAIL_NOTIFICATION.name());
        assertThat(notificationRequest.getBody().getTemplateParams()).isNotEmpty();
        assertThat(notificationRequest.getBody().getTemplateParams().get(NotificationEventParams.MASTER_REFERENCE_NUMBER)).isEqualTo(MASTER_REFERENCE_ID);
        assertThat(notificationRequest.getAttachments().get(0).getAttachmentUrl()).isNotEmpty().isEqualTo("attachementurl");
        assertThat(notificationRequest.getAttachments().get(0).getAedEndpoint()).isNotEmpty().isEqualTo("aedEndpoint");
    }

    @Test
    void notifyError() {

        try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());
            Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
            BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
            BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
            StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
            bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
            bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
            EmailNotificationInput nonStpEmailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
            BgosException exception = assertThrows(BgosException.class, () -> beneficiaryEmailNotificationProducer.notify(nonStpEmailNotificationInput));
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
        }
    }

    @Test
    void notifyCCAddressCheck() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setMasterReferenceNumber(MASTER_REFERENCE_ID);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        document.setDocumentType(DocumentType.BG_FINAL);
        document.setStatus(DocumentStatus.READY);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        bankGuaranteeRequest.getBgRequest().setStpResultDataSet(stpResultDataset);
        EmailNotificationInput nonStpEmailNotificationInput = MockHelper.getNonStpNotificationInput(bankGuaranteeRequest, List.of(document));
        doNothing().when(engagementSuiteGateway).send(any(), any(), any());
        ArgumentCaptor<NotificationEvent> argumentCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

        beneficiaryEmailNotificationProducer.notify(nonStpEmailNotificationInput);
        beneficiaryEmailNotificationProducer.notify(nonStpEmailNotificationInput);

        verify(engagementSuiteGateway, times(2)).send(argumentCaptor.capture(), any(), any());
        List<NotificationEvent> allValues = argumentCaptor.getAllValues();

        List<String> emailDigitalAddress = List.of(CommonUtils.getEmailDigitalAddress(bankGuaranteeRequestData.getInstructingParty().getIndividual().getDigitalAddresses()));

        NotificationEvent notificationEventFirst = allValues.get(0);
        assertThat(notificationEventFirst.getBody().getCcAddresses().size()).isEqualTo(1);
        assertThat(notificationEventFirst.getBody().getCcAddresses()).isEqualTo(emailDigitalAddress);

        NotificationEvent notificationEventSecond = allValues.get(1);
        assertThat(notificationEventSecond.getBody().getCcAddresses().size()).isEqualTo(1);
        assertThat(notificationEventSecond.getBody().getCcAddresses()).isEqualTo(emailDigitalAddress);

    }

}

