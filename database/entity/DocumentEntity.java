package com.ing.bankguarantees.database.entity;

import com.ing.bankguarantees.models.enums.DocumentStatus;
import com.ing.bankguarantees.models.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

/**
 * Document entity - used to map the table DOCUMENT
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DOCUMENTS")
public class DocumentEntity{

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "DOC_ID")
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "DOC_TYPE")
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private DocumentStatus status;

    @Column(name = "REQUEST_ID")
    private String requestId;

    @Column(name = "ORGANISATION_ID")
    private String organisationId;

    @Column(name = "TMS_CREATE", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "TMS_UPDATE")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

}
