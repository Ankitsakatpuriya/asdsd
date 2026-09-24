package com.ing.bankguarantees.database;

import com.ing.bankguarantees.database.entity.CustomDocumentEntity;
import com.ing.bankguarantees.database.repository.CustomDocumentRepository;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.CustomDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.mapper.CustomDocumentMapper.CUSTOM_DOCUMENT_MAPPER;

@Slf4j
@Repository
public class CustomDocumentDao {

    private final CustomDocumentRepository customDocumentRepository;
    private final ExecutorService executorService;

    public CustomDocumentDao(CustomDocumentRepository customDocumentRepository, @Qualifier("workStealingPool") ExecutorService executorService) {
        this.customDocumentRepository = customDocumentRepository;
        this.executorService = executorService;
    }

    @Transactional
    public CustomDocument saveDocument(CustomDocument customDocument) {
        try {
            CustomDocumentEntity documentEntity = customDocumentRepository.save(CUSTOM_DOCUMENT_MAPPER.toEntity(customDocument));
            log.info("Custom document entity saved in database with doc id {}", documentEntity.getId());
            return CUSTOM_DOCUMENT_MAPPER.toDomainModel(documentEntity);
        } catch (Exception ex) {
            log.error("Notifiable Database exception: Error occurred while saving DOCUMENT for  id {}", customDocument.getId());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private Optional<CustomDocument> findDocumentEntityByRequestId(String requestID) {

        try {
            return customDocumentRepository.findByRequestId(requestID).map(CUSTOM_DOCUMENT_MAPPER::toDomainModel);
        } catch (Exception ex) {
            log.error(""" 
                    Notifiable Database exception: Error occurred when fetching custom document with request id {}\
                     with exception""", requestID);
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


    public CompletableFuture<CustomDocument> getDocumentEntityByRequestId(String requestId) {
        return CompletableFuture.supplyAsync(() ->
                findDocumentEntityByRequestId(requestId)
                        .orElseThrow(() -> {
                            log.error("Informational Database exception: Document Entity is not found for request id {}", requestId);
                            return new BgosException(ErrorCode.ENTITY_NOT_FOUND);
                        }), executorService);
    }
}
