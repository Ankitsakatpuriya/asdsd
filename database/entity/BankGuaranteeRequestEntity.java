package com.ing.bankguarantees.database.entity;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.database.transformer.BankGuaranteeRequestTransformer;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BG_REQUEST")
public class BankGuaranteeRequestEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "REQUEST_ID")
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private BankGuaranteeRequestStatus status;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "INDIVIDUAL_ID")
    private String individualId;

    @Column(name = "ORGANISATION_ID")
    private String organisationId;

    @Lob
    @Column(name = "BG_REQUEST")
    @Convert(converter = BankGuaranteeRequestTransformer.class)
    private BankGuaranteeRequestData bgRequest;

    @Column(name = "MASTER_REF_NUMBER")
    private String masterReferenceNumber;

    @Column(name = "TMS_CREATE", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "TMS_UPDATE")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "PEGA_CASE_ID")
    private String pegaCaseId;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

}
