package com.ing.bankguarantees.database.repository;

import com.ing.bankguarantees.database.entity.DocumentEntity;
import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Transactional
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    @Modifying
    @Query("update DocumentEntity a set a.status = :status, a.documentId = :documentId, a.updatedBy = :updatedBy where a.id = :id")
    void updateDocumentIdAndStatusById(@Param(value = "id") String id, @Param(value = "documentId") String documentId,
                                       @Param(value = "status") DocumentStatus status, @Param(value = "updatedBy") String updatedBy);

    @Modifying
    @Query("update DocumentEntity a set a.status = :status where a.requestId = :requestId")
    void updateStatusByRequestId(@Param(value = "requestId") String requestId, @Param(value = "status") DocumentStatus status);


    Optional<DocumentEntity> findByRequestIdAndDocumentType(@Param("requestId") String requestId, @Param("documentType") DocumentType documentType);

    Optional<List<DocumentEntity>> findByRequestId(@Param("requestId") String requestId);

    @Modifying
    CompletableFuture<Integer> deleteByRequestId(String requestId);
}
