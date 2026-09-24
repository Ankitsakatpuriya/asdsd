package com.ing.bankguarantees.remote.rest.dar.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.Builder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record DarV3Request(

        GeneralRequest general,
        ProductRequest product,
        List<SignerRequest> signers,
        List<DocumentRequest> documents,
        List<ActionRequest> actions,
        ClientCallbackRequest clientCallback,
        List<FrontEndCallbackRequest> frontEndCallbacks,
        NotificationRequest notification,
        List<TranslationRequest> translations
) {

    @Builder
    public record GeneralRequest(

            String referenceId,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_FORMAT)
            @JsonDeserialize(using = LocalDateDeserializer.class)
            @JsonSerialize(using = LocalDateSerializer.class)
            LocalDate expiryDate,

            String userFallbackLanguage,
            List<String> userSupportedLanguages,
            TranslationCodeV3Request referenceReplacementText

    ) {

    }

    @Builder
    public record ProductRequest(

            String countryCode,
            String businessLine,
            String serviceActivity,
            String serviceName,
            String tribe,
            Segment segment,
            TranslationCodeV3Request productType

    ) {

    }

    @Builder
    public record SignerRequest(

            String code,
            SignerType type,
            ActorIdType idType,
            String idNumber,
            String language,
            String frontEndCallbackCode,
            String emailAddress

    ) {

    }

    @Builder
    public record DocumentRequest(

            String code,
            String link,
            TranslationCodeV3Request name,
            String language
    ) {

    }

    @Builder
    public record ActionRequest(

            String code,
            DocumentActionType type,
            List<String> signerCodes,
            List<String> documentCodes,
            boolean main,
            boolean mandatory,
            SignActionRequest signing

    ) {

    }

    @Builder
    public record SignActionRequest(
            TranslationCodeV3Request signatoryNotice,
            SignatoryType signatoryNoticeType,
            String signingMean

    ) {

    }

    @Builder
    public record FrontEndCallbackRequest(
            String code,
            String redirectUrl,
            TranslationCodeV3Request redirectText,
            String cancelUrl,
            String closeUrl,
            List<String> exemptActorIds,
            List<String> exemptSignerIds

    ) {

    }

    @Builder
    public record EmailNotificationRequest(

            String communicationLanguage,
            Set<TriggerStatusV3> triggerStatus,
            String actorId,
            String businessMessage,
            List<String> signerCodes,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConstantUtils.DATE_TIME_FORMAT)
            @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
            @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
            List<LocalDateTime> notificationDates,
            String emailAddress
    ) {

    }


    @Builder
    public record NotificationRequest(CustomerNotificationsRequest customerNotifications) {

    }

    @Builder
    public record CustomerNotificationsRequest(List<EmailNotificationRequest> email) {

    }

    @Builder
    public record ClientCallbackRequest(
            String topic,
            Set<TriggerStatusV3> triggerStatus,
            Boolean detailedCallbackTopic,
            List<IntermediateCallbackRequest> intermediateCallbacks
    ) {

    }

    @Builder
    public record IntermediateCallbackRequest(String signerCode, Set<IntermediateStatus> triggerIntermediateStatus) {

    }

    @Builder
    public record TranslationCodeV3Request(Integer translationCode) {

    }
}

