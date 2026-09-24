package com.ing.bankguarantees.database.repository;

import com.ing.bankguarantees.database.entity.BankGuaranteeRequestEntity;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Transactional
public interface BankGuaranteeRequestRepository extends JpaRepository<BankGuaranteeRequestEntity, String> {

    @Modifying
    @Query("update BankGuaranteeRequestEntity a set a.status = :status where a.requestId = :requestId")
    void updateStatusById(@Param(value = "requestId") String requestId, @Param("status") BankGuaranteeRequestStatus status);

    Optional<BankGuaranteeRequestEntity> findByRequestId(@Param("requestId") String requestId);


    Optional<List<BankGuaranteeRequestEntity>> findByStatusInAndUpdatedAtLessThan(@Param(value = "status") List<BankGuaranteeRequestStatus> status,
                                                                                  @Param(value = "updatedAt") Instant updatedAt);

    @Modifying
    CompletableFuture<Integer> deleteByRequestId(String requestId);

    Optional<List<BankGuaranteeRequestEntity>> findByUpdatedAtLessThan(@Param(value = "updatedAt") Instant updatedAt);

    Optional<List<BankGuaranteeRequestEntity>> findByUpdatedAtGreaterThan(@Param(value = "updatedAt") Instant updatedAt);

    Optional<BankGuaranteeRequestEntity> findByMasterReferenceNumber(@Param("masterReferenceNumber") String masterReferenceNumber);

    Optional<List<BankGuaranteeRequestEntity>> findByStatus(@Param(value = "status") BankGuaranteeRequestStatus status);
}
