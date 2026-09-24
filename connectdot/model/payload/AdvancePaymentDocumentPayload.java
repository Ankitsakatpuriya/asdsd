package com.ing.bankguarantees.remote.rest.connectdot.model.payload;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdvancePaymentDocumentPayload extends BaseDocumentPayload {


    private String contractDescription;

    @JsonProperty("contractRef")
    private String contractReferenceNumber;

    @JsonProperty("bgexpirtdate")
    private String bgExpiry;

    @JsonProperty("bgexpiryCode")
    private String expiryCode;

    @JsonProperty("bankaccountCredit")
    private String bankAccountCredit;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("bgmaturitydate")
    private LocalDate bgMaturityDate;

    private ApplicantDocumentPayload applicant;

    @JsonProperty("benificiary")
    private BeneficiaryDocumentPayload beneficiary;
}
