package com.ing.bankguarantees.database.repository;

import com.ing.bankguarantees.database.entity.CustomDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CustomDocumentRepository extends JpaRepository<CustomDocumentEntity, String> {

    Optional<CustomDocumentEntity> findByRequestId(@Param("requestId") String requestId);
}
