package com.ing.bankguarantees.database.repository;

import com.ing.bankguarantees.database.entity.ReportingEntity;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public interface ReportingRepository extends JpaRepository<ReportingEntity, String> {

    @Modifying
    @Query("update ReportingEntity a set a.status = :status where a.requestId = :requestId")
    void updateStatusByRequestId(@Param(value = "requestId") String requestId, @Param("status") BankGuaranteeRequestStatus status);

    @Modifying
    @Query("update ReportingEntity a set a.status = :status, a.stp = :stp where a.requestId = :requestId")
    void updateStatusAndStpByRequestId(@Param(value = "requestId") String requestId,
                                       @Param("status") BankGuaranteeRequestStatus status,
                                       @Param("stp") String stp);
}
