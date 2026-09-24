package com.ing.bankguarantees.database.entity;

import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Document entity - used to map the table REPORTING
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "REPORTING")
public class ReportingEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "REQUEST_ID")
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "REPORTING_STATUS")
    private BankGuaranteeRequestStatus status;

    @Column(name = "STP")
    private String stp;

    @CreationTimestamp
    @Column(name = "TMS_CREATE", updatable = false)
    private Instant createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "TMS_UPDATE")
    private Instant updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

}
