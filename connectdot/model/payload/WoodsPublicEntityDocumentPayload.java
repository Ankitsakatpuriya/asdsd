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
public class WoodsPublicEntityDocumentPayload extends BaseDocumentPayload {

    private String contractDescription;

    @JsonProperty("contractcity")
    private String contractCity;

    @JsonProperty("contractAmount")
    private Double salePrice;

    @JsonProperty("cash")
    private Double cash;

    @JsonProperty("trancheOne")
    private Double trancheOne;

    @JsonProperty("trancheTwo")
    private Double trancheTwo;

    @JsonProperty("trancheThree")
    private Double trancheThree;

    @JsonProperty("trancheFour")
    private Double trancheFour;

    private Double woodMinAmount;

    private String replacePromiseId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate deadlineOne;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate deadlineTwo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate deadlineThree;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate deadlineFour;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("contractsigndate")
    private LocalDate contractSignDate;

    private ApplicantDocumentPayload applicant;

    @JsonProperty("benificiary")
    private BeneficiaryDocumentPayload beneficiary;

}
