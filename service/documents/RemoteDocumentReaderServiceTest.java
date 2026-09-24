package com.ing.bankguarantees.service.documents;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.bankguarantees.database.BankGuaranteeRequestDao;
import com.ing.bankguarantees.database.DocumentDao;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.AccessTokenUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class RemoteDocumentReaderServiceTest {

    private static final String DOCUMENT_ID = "0283f2hjxy0";
    private static final String REQUESTER_ID = "2949e88c-943e-43fd-82d0-3342652a79be";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String SESSION_ID = UUID.randomUUID().toString();

    @Mock
    private DocumentDao documentDao;

    @Mock
    private ClientGateway<String, ByteArrayResource, byte[]> requestDocumentClientGateway;

    @Mock
    private BankGuaranteeRequestDao bankGuaranteeRequestDao;


    @InjectMocks
    private RemoteDocumentReaderService remoteDocumentReaderService;


    @Test
    void checkGetDocumentPositive() {
        String requestId = UUID.randomUUID().toString();
        byte[] bytes = MockHelper.readFile(BankGuaranteeCode.PUBLIC_CONTRACT, "en");
        ByteArrayResource byteArrayResource = new ByteArrayResource(bytes);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.CREATED);
        document.setStatus(DocumentStatus.READY);
        AccessToken accessToken = MockHelper.getAccessToken();
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        given(requestDocumentClientGateway.performRequest(any())).willReturn(CompletableFuture.completedFuture(byteArrayResource));
        Pair<ByteArrayResource, String> remoteDocumentResponse = remoteDocumentReaderService.getDocument(accessToken, requestId, DocumentType.BG_DRAFT).join();
        assertThat(remoteDocumentResponse).isNotNull();
        assertThat(remoteDocumentResponse.getLeft()).isNotNull();
    }

    @Test
    void checkGetDocumentInvalidRequester() {
        String requestId = UUID.randomUUID().toString();
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        document.setStatus(DocumentStatus.READY);
        AccessToken accessToken = MockHelper.getAccessToken();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.CREATED);
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        try (MockedStatic<AccessTokenUtil> mockStatic = mockStatic(AccessTokenUtil.class)) {
            AccessTokenUtil.getRequesterPersonId(accessToken);
            mockStatic.when(() -> AccessTokenUtil.getRequesterPersonId(any())).thenReturn("qwert");
            var documentFuture = remoteDocumentReaderService.getDocument(accessToken, requestId, DocumentType.BG_DRAFT);
            CompletionException exception = assertThrows(CompletionException.class, documentFuture::join);
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNotNull();
            BgosException cause = (BgosException) exception.getCause();
            assertThat(cause.getMessage()).isEqualTo("BGOS-00-021");
        }
    }

    @ParameterizedTest
    @MethodSource(value = "docStatuses")
    void checkGetDocumentEmptyResponse() {
        String requestId = UUID.randomUUID().toString();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        document.setStatus(DocumentStatus.ERROR);
        bankGuaranteeRequest.setStatus(BankGuaranteeRequestStatus.ERROR);
        AccessToken accessToken = MockHelper.getAccessToken();
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        var remoteDocumentResponse = remoteDocumentReaderService.getDocument(accessToken, requestId, DocumentType.BG_DRAFT);
        CompletionException exception = assertThrows(CompletionException.class, remoteDocumentResponse::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-022");
    }

    @Test
    void checkGetDocumentEmptyResponseForDraftState() {
        String requestId = UUID.randomUUID().toString();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, REQUESTER_ID, SESSION_ID, REQUESTER_ID);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        AccessToken accessToken = MockHelper.getAccessToken();
        given(bankGuaranteeRequestDao.getBankGuaranteeEntity(any())).willReturn(CompletableFuture.completedFuture(bankGuaranteeRequest));
        given(documentDao.getDocumentEntityByRequestIdAndDocType(any(), any())).willReturn(CompletableFuture.completedFuture(document));
        var remoteDocumentResponse = remoteDocumentReaderService.getDocument(accessToken, requestId, DocumentType.BG_DRAFT);
        CompletionException exception = assertThrows(CompletionException.class, remoteDocumentResponse::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-022");
    }

    private static Stream<DocumentStatus> docStatuses() {

        return Stream.of(DocumentStatus.NEW, DocumentStatus.ERROR);
    }


}
