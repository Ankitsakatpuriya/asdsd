package com.ing.bankguarantees.remote.rest.dar.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DarRequest {

    private String agreementId;

    private String businessLine;

    private TranslationCodeRequest agreementIdReplacement;

    private TranslationCodeRequest productType;
    private Segment segment;
    private ActorIdType actorIdType;
    private String reminderDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate expirationDate;

    private String legalEntityId;
    private Boolean checkCorporateMandate;
    private Integer mandatorySignatureCount;
    private String userFallbackLanguage;
    private List<String> userSupportedLanguages;
    private List<TranslationRequest> translations;
    private Boolean blueInk;
    private String countryCode;
    private List<DocumentActionRequest> documentActions;
    private NotificationRequest notifications;
    private CallbackRequest callback;
    private ClientIdentificationRequest clientIdentificationRequest;
    private FrontEndCallbackRequest frontEndCallback;


}

