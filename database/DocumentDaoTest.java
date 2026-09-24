package com.ing.bankguarantees.database;

import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.database.entity.DocumentEntity;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.database.repository.DocumentRepository;
import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.mapper.DocumentMapper.DOCUMENT_MAPPER;
import static com.ing.bankguarantees.util.MockHelper.getDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentDaoTest {

    private final static String ID = "82c2d18e-3609-4514-9542-28e29c7c2c02";
    public static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    public static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    public static final String DOC_ID = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    @Mock
    private DocumentRepository documentRepository;

    private DocumentDao documentDao;

    @BeforeEach
    void setUp() {
        ExecutorService executorService = ExecutorConfig.workStealingPool();
        documentDao = new DocumentDao(documentRepository, executorService);
    }

    @Test
    public void saveDocumentPositive() {

        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocationOnMock -> {
            DocumentEntity savedEntity = invocationOnMock.getArgument(0);
            savedEntity.setId(ID);
            return savedEntity;
        });
        Document document = getDocument(UUID_ORG, DOC_ID, REQUESTER_ID);
        Document entity = documentDao.saveDocument(document);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getStatus()).isEqualTo(DocumentStatus.NEW);
        assertThat(entity.getCreatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getUpdatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getOrganisationId()).isEqualTo(UUID_ORG);
        assertThat(entity.getRequestId()).isNotEmpty();
    }

    @Test
    public void saveBankGuaranteeRequestError() {

        when(documentRepository.save(any(DocumentEntity.class))).thenThrow(RuntimeException.class);
        Document document = getDocument(UUID_ORG, DOC_ID, REQUESTER_ID);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> documentDao.saveDocument(document));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    public void updateStatusByRequestIdPositive() {
        String requestId = UUID.randomUUID().toString();
        doNothing().when(documentRepository).updateStatusByRequestId(requestId, DocumentStatus.NEW);
        documentDao.updateStatusByRequestId(requestId, DocumentStatus.NEW);
        verify(documentRepository, times(1)).updateStatusByRequestId(requestId, DocumentStatus.NEW);

    }

    @Test
    public void updateStatusByRequestIdError() {
        String requestId = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(documentRepository).updateStatusByRequestId(requestId, DocumentStatus.NEW);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> documentDao.updateStatusByRequestId(requestId, DocumentStatus.NEW));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }

    @Test
    public void updateDocumentIdAndStatusForDocumentPositive() {

        doNothing().when(documentRepository).updateDocumentIdAndStatusById(ID, DOC_ID, DocumentStatus.NEW, REQUESTER_ID);
        documentDao.updateDocumentIdAndStatusForDocument(ID, DocumentStatus.NEW, DOC_ID, REQUESTER_ID);
        verify(documentRepository, times(1)).updateDocumentIdAndStatusById(ID, DOC_ID, DocumentStatus.NEW, REQUESTER_ID);

    }

    @Test
    public void updateDocumentIdAndStatusForDocumentError() {
        doThrow(RuntimeException.class).when(documentRepository).updateDocumentIdAndStatusById(ID, DOC_ID, DocumentStatus.NEW, REQUESTER_ID);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> documentDao.updateDocumentIdAndStatusForDocument(ID, DocumentStatus.NEW, DOC_ID, REQUESTER_ID));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    public void findDocumentEntityByRequestIdAndTypePositive() {
        String requestId = UUID.randomUUID().toString();
        Document document = getDocument(UUID_ORG, DOC_ID, REQUESTER_ID);
        DocumentEntity documentEntity = DOCUMENT_MAPPER.toEntity(document);
        given(documentRepository.findByRequestIdAndDocumentType(requestId, DocumentType.BG_DRAFT)).willReturn(Optional.of(documentEntity));
        Optional<Document> documentFuture = documentDao.findDocumentEntityByRequestIdAndType(requestId, DocumentType.BG_DRAFT);
        assertThat(documentFuture).isPresent();
        assertThat(documentFuture.get()).isEqualTo(document);
    }

    @Test
    public void findDocumentEntityByRequestIdAndTypeError() {
        String requestId = UUID.randomUUID().toString();
        given(documentRepository.findByRequestIdAndDocumentType(requestId, DocumentType.BG_DRAFT)).willThrow(RuntimeException.class);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> documentDao.findDocumentEntityByRequestIdAndType(requestId, DocumentType.BG_DRAFT));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    public void getDocumentEntityByRequestIdAndDocTypePositive() {
        String requestId = UUID.randomUUID().toString();
        Document document = getDocument(UUID_ORG, DOC_ID, REQUESTER_ID);
        DocumentEntity documentEntity = DOCUMENT_MAPPER.toEntity(document);
        given(documentRepository.findByRequestIdAndDocumentType(requestId, DocumentType.BG_DRAFT)).willReturn(Optional.of(documentEntity));
        Document actualResult = documentDao.getDocumentEntityByRequestIdAndDocType(requestId, DocumentType.BG_DRAFT).join();
        assertThat(actualResult).isNotNull();
        assertThat(actualResult).isEqualTo(document);
    }

    @Test
    public void getDocumentEntityByRequestIdAndDocTypeError() {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Document> entityFuture = documentDao.getDocumentEntityByRequestIdAndDocType(requestId, DocumentType.BG_DRAFT);
        CompletionException exception = assertThrows(CompletionException.class, entityFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-007");
    }

    @Test
    public void getDocumentsRequestIdPositive() {
        String requestId = UUID.randomUUID().toString();
        Document document = MockHelper.getDocument(UUID_ORG, DOC_ID, REQUESTER_ID);
        List<DocumentEntity> bgEntityList = List.of(DOCUMENT_MAPPER.toEntity(document));
        given(documentRepository.findByRequestId(requestId)).willReturn(Optional.of(bgEntityList));
        List<Document> actualResult = documentDao.getDocumentsRequestId(requestId).join();
        assertThat(actualResult).isNotEmpty();
        assertThat(actualResult).isEqualTo(List.of(document));
    }

    @Test
    public void getDocumentsRequestIdError() {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<List<Document>> entityFuture = documentDao.getDocumentsRequestId(requestId);
        CompletionException exception = assertThrows(CompletionException.class, entityFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-007");
    }

    @Test
    public void deleteByRequestId() {
        String requestId = UUID.randomUUID().toString();
        given(documentRepository.deleteByRequestId(requestId)).willReturn(CompletableFuture.completedFuture(2));
        documentDao.deleteByRequestId(requestId);
        verify(documentRepository, times(1)).deleteByRequestId(requestId);
    }
}
