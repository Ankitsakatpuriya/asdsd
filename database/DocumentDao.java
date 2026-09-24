package com.ing.bankguarantees.database;

import com.ing.bankguarantees.database.entity.DocumentEntity;
import com.ing.bankguarantees.database.repository.DocumentRepository;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.mapper.DocumentMapper.DOCUMENT_MAPPER;

@Slf4j
@Repository
public class DocumentDao {

    private final DocumentRepository documentRepository;
    private final ExecutorService executorService;

    public DocumentDao(DocumentRepository documentRepository,
                       @Qualifier("workStealingPool") ExecutorService executorService) {
        this.documentRepository = documentRepository;
        this.executorService = executorService;
    }

    @Transactional
    public Document saveDocument(Document document) {
        try {
            DocumentEntity documentEntity = documentRepository.save(DOCUMENT_MAPPER.toEntity(document));
            log.info("Document entity saved in database with doc id {}, doc type {}", documentEntity.getId(), documentEntity.getDocumentType());
            return DOCUMENT_MAPPER.toDomainModel(documentEntity);
        } catch (Exception ex) {
            log.error("Notifiable Database exception: Error occurred while saving DOCUMENT for  id {} type {}",
                    document.getId(), document.getDocumentType());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    @Transactional
    public void updateStatusByRequestId(String requestId, DocumentStatus documentStatus) {
        try {
            documentRepository.updateStatusByRequestId(requestId, documentStatus);
            log.info("Updated document for requestId {}, with status {}", requestId, documentStatus);
        } catch (Exception ex) {
            log.error("""
                    Notifiable Database exception: Error occurred when updating status to {} \
                     for document requestId {} with exception""", documentStatus, requestId);

            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    @Transactional
    public void updateDocumentIdAndStatusForDocument(String id, DocumentStatus documentStatus, String documentId, String profileId) {
        try {
            documentRepository.updateDocumentIdAndStatusById(id, documentId, documentStatus, profileId);
            log.info("Updated document for id {}, profile id {} with status {} and documentId {}",
                    id, profileId, documentStatus, documentId);
        } catch (Exception ex) {
            log.error("""
                    Notifiable Database exception: Error occurred when updating status to {} and document id {} \
                    for document {} and profile id {} with exception""", documentStatus, documentId, id, profileId);
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    public Optional<Document> findDocumentEntityByRequestIdAndType(String requestID, DocumentType documentType) {

        try {
            return documentRepository.findByRequestIdAndDocumentType(requestID, documentType).map(DOCUMENT_MAPPER::toDomainModel);
        } catch (Exception ex) {
            log.error(""" 
                    Notifiable Database exception: Error occurred when fetching document with loan application Id {}\
                     document type {} with exception""", requestID, documentType);
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    public CompletableFuture<Document> getDocumentEntityByRequestIdAndDocType(String requestId, DocumentType documentType) {
        return CompletableFuture.supplyAsync(() ->
                findDocumentEntityByRequestIdAndType(requestId, documentType)
                        .orElseThrow(() -> {
                            log.error("Informational Database exception: Document Entity is not found for request id {} and Document Type {}", requestId, documentType);
                            return new BgosException(ErrorCode.ENTITY_NOT_FOUND);
                        }), executorService);
    }

    public CompletableFuture<List<Document>> getDocumentsRequestId(String requestId) {
        return CompletableFuture.supplyAsync(() -> {
            List<DocumentEntity> documentEntities = documentRepository.findByRequestId(requestId).
                    orElseThrow(() -> {
                        log.error("Informational Database exception: Document Entities is not found for  Request id {} ", requestId);
                        return new BgosException(ErrorCode.ENTITY_NOT_FOUND);
                    });
            return DOCUMENT_MAPPER.toDomainModelList(documentEntities);
        }, executorService);
    }

    public CompletableFuture<Integer> deleteByRequestId(String requestId) {
        var deletedRecords = documentRepository.deleteByRequestId(requestId);
        log.info("""
                DocumentPersistentService [deleteByRequestId] deleting {} bank guarantee \
                documents record for request id  {}""", deletedRecords, requestId);
        return deletedRecords;
    }
}
