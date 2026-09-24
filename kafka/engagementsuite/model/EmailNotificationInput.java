package com.ing.bankguarantees.remote.kafka.engagementsuite.model;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationInput {
    private BankGuaranteeRequest bankGuaranteeRequest;
    private List<Document> documentList;
    private String traceId;
    private String sessionId;
    private String profileId;
    private String spanId;
    private String parentId;
    private LegalRepresentativeData receiverLegalRep;
}
