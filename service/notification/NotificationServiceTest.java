package com.ing.bankguarantees.service.notification;

import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.producer.*;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private NonStpEmailNotificationProducer nonStpEmailNotificationProducer;

    @Mock
    private CustomerEmailNotificationProducer customerEmailNotificationProducer;

    @Mock
    private BeneficiaryEmailNotificationProducer beneficiaryEmailNotificationProducer;

    @Mock
    private FulfilmentFailureEmailNotificationProducer fulfilmentFailureEmailNotificationProducer;

    @Mock
    private StpResultsEmailNotificationProducer stpResultsEmailNotificationProducer;

    @Mock
    private SignCompletionEmailNotificationProducer signCompletionEmailNotificationProducer;

    @Mock
    private RequesterPartiallySignedEmailNotificationProducer requesterPartiallySignedEmailNotificationProducer;

    @Mock
    private SignatureReminderEmailNotificationProducer signatureReminderEmailNotificationProducer;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;

    @InjectMocks
    private NotificationService notificationService;

    public static ConditionFactory WAIT = await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofSeconds(1))
            .pollDelay(Duration.ofSeconds(1));


    @Test
    void checkSendStpNotificationToBoth() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        Document bgFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        bgFinal.setDocumentType(DocumentType.BG_FINAL);
        Document contractDraft = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractDraft.setDocumentType(DocumentType.CONTRACT);
        Document contractFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractFinal.setDocumentType(DocumentType.CONTRACT_FINAL);
        List<Document> docList = List.of(document, bgFinal, contractFinal, contractDraft);

        notificationService.sendStpNotification(bankGuaranteeRequest, docList);

        WAIT.untilAsserted(() -> {
            ArgumentCaptor<EmailNotificationInput> argumentCaptor = ArgumentCaptor.forClass(EmailNotificationInput.class);
            ArgumentCaptor<EmailNotificationInput> argumentCaptor2 = ArgumentCaptor.forClass(EmailNotificationInput.class);
            verify(customerEmailNotificationProducer, times(1)).notify(argumentCaptor.capture());
            verify(beneficiaryEmailNotificationProducer, times(1)).notify(argumentCaptor2.capture());
            EmailNotificationInput notificationInput = argumentCaptor.getValue();
            EmailNotificationInput notificationInput2 = argumentCaptor2.getValue();
            List<Document> finalDocuments = filterDocumentByType(List.of(DocumentType.BG_FINAL, DocumentType.CONTRACT_FINAL), docList);
            List<Document> finalBgDocs = filterDocumentByType(List.of(DocumentType.BG_FINAL), docList);
            assertThat(notificationInput.getBankGuaranteeRequest()).isEqualTo(bankGuaranteeRequest);
            assertThat(notificationInput.getDocumentList()).isEqualTo(finalDocuments);
            assertThat(notificationInput2.getDocumentList()).isEqualTo(finalBgDocs);
        });

    }

    @Test
    void checkSendStpNotificationToCustomer() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getDeliveryInformation().setRecipient(BankGuaranteeRecipient.ME);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        Document bgFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        bgFinal.setDocumentType(DocumentType.BG_FINAL);
        Document contractDraft = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractDraft.setDocumentType(DocumentType.CONTRACT);
        Document contractFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractFinal.setDocumentType(DocumentType.CONTRACT_FINAL);
        List<Document> docList = List.of(document, bgFinal, contractFinal, contractDraft);

        notificationService.sendStpNotification(bankGuaranteeRequest, docList);

        WAIT.untilAsserted(() -> {
            ArgumentCaptor<EmailNotificationInput> argumentCaptor = ArgumentCaptor.forClass(EmailNotificationInput.class);
            verify(customerEmailNotificationProducer, times(1)).notify(argumentCaptor.capture());
            verify(beneficiaryEmailNotificationProducer, never()).notify(any());
            EmailNotificationInput notificationInput = argumentCaptor.getValue();
            List<Document> finalDocuments = filterDocumentByType(List.of(DocumentType.BG_FINAL, DocumentType.CONTRACT_FINAL), docList);
            assertThat(notificationInput.getBankGuaranteeRequest()).isEqualTo(bankGuaranteeRequest);
            assertThat(notificationInput.getDocumentList()).isEqualTo(finalDocuments);
        });

    }


    @Test
    void checkSendNonStpToCustomer() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(MockHelper.createStpResultDataset(BG_STP_RESULT_FILE));
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        notificationService.sendFulfilmentFailure(bankGuaranteeRequest);

        WAIT.untilAsserted(() -> {
            ArgumentCaptor<EmailNotificationInput> argumentCaptor = ArgumentCaptor.forClass(EmailNotificationInput.class);
            ArgumentCaptor<EmailNotificationInput> argumentCaptor2 = ArgumentCaptor.forClass(EmailNotificationInput.class);
            verify(fulfilmentFailureEmailNotificationProducer, times(1)).notify(argumentCaptor.capture());
            verify(stpResultsEmailNotificationProducer, times(1)).notify(argumentCaptor2.capture());
            EmailNotificationInput notificationInput = argumentCaptor.getValue();
            EmailNotificationInput notificationInput2 = argumentCaptor2.getValue();
            assertThat(notificationInput.getBankGuaranteeRequest()).isEqualTo(bankGuaranteeRequest);
            assertThat(notificationInput2.getBankGuaranteeRequest()).isEqualTo(bankGuaranteeRequest);
            assertThat(notificationInput.getDocumentList()).isEqualTo(null);
            assertThat(notificationInput2.getDocumentList()).isEqualTo(null);
        });
    }

    @Test
    void checkSendNonStpToClt() {


        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        Document bgFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        bgFinal.setDocumentType(DocumentType.BG_FINAL);
        Document contractDraft = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractDraft.setDocumentType(DocumentType.CONTRACT);
        Document contractFinal = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        contractFinal.setDocumentType(DocumentType.CONTRACT_FINAL);
        List<Document> docList = List.of(document, bgFinal, contractFinal, contractDraft);
        notificationService.sendNonStpEmail(bankGuaranteeRequest, docList);

        WAIT.untilAsserted(() -> {
            ArgumentCaptor<EmailNotificationInput> argumentCaptor2 = ArgumentCaptor.forClass(EmailNotificationInput.class);
            verify(nonStpEmailNotificationProducer, times(1)).notify(argumentCaptor2.capture());
            EmailNotificationInput notificationInput2 = argumentCaptor2.getValue();
            assertThat(notificationInput2.getBankGuaranteeRequest()).isEqualTo(bankGuaranteeRequest);
            assertThat(notificationInput2.getDocumentList()).isEqualTo(docList);
        });


    }

    @Test
    void checkSendSignCompletionEmail() {

        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setSigned(true);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        notificationService.sendSignCompletionEmail(bankGuaranteeRequest);

        WAIT.untilAsserted(() -> {
            verify(signCompletionEmailNotificationProducer, times(1)).notify(any());
        });
    }

    @Test
    void checkSendReminderToRequesterEmail() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.setSigned(true);
        bankGuaranteeRequestData.setSignExpiryDate(LocalDate.now().plusDays(2));
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        notificationService.sendReminderToRequester(bankGuaranteeRequest);

        WAIT.untilAsserted(() -> {
            verify(signatureReminderEmailNotificationProducer, times(1)).notify(any());
        });
    }

    @Test
    void checkSendPartiallySignedEmailEmail() {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        notificationService.sendPartiallySignedEmail(bankGuaranteeRequest);
        WAIT.untilAsserted(() -> {
            verify(requesterPartiallySignedEmailNotificationProducer, times(1)).notify(any());
        });
    }


    private List<Document> filterDocumentByType(List<DocumentType> documentTypes, List<Document> documents) {

        return documents.stream()
                .filter(document -> documentTypes.contains(document.getDocumentType()))
                .toList();
    }


}

