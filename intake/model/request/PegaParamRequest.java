package com.ing.bankguarantees.remote.rest.intake.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.Builder;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;

import java.time.LocalDate;

@Builder
public record PegaParamRequest(

        String team,
        String existingCreditLine,
        String indirectGuarantee,
        String decisionSheetNeeded,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.PEGA_DATE_FORMAT)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        LocalDate decisionSheetReceived,

        String otherDocumentsNeeded,
        String orfAgreementReceived,
        String customerSegmentation,
        ExtraInfoRequest extraInfo,
        DocumentumIdentifierRequest documentumIdentifiers
) {
    @Builder
    public record DocumentumIdentifierRequest(String requestDossierId, String agreementDossierId) {
    }

    @Builder
    public record ExtraInfoRequest(DecisionRequest decision) {
    }

    @Builder
    public record DecisionRequest(String pdlResult, String creditLineResult) {
    }
}
