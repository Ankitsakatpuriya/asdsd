package com.ing.bankguarantees.remote.rest.dar.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.dar.DarProperties;
import com.ing.bankguarantees.remote.rest.dar.model.request.*;
import com.ing.bankguarantees.remote.utils.TranslationsLoader;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ing.bankguarantees.models.enums.DocumentType.BG_DRAFT;
import static com.ing.bankguarantees.remote.rest.dar.model.request.ActorIdType.UUID;
import static com.ing.bankguarantees.remote.rest.dar.model.request.CountryCodes.*;
import static com.ing.bankguarantees.remote.rest.dar.model.request.DocumentActionType.SIGN;
import static com.ing.bankguarantees.remote.rest.dar.model.request.Segment.BUSINESS;
import static com.ing.bankguarantees.remote.rest.dar.model.request.SignatoryType.CHECKBOX;


@Slf4j
@Component
@RequiredArgsConstructor
public class DarRequestMapper {

    @Value("${bgos.docsign-signingmean}")
    private String signingMean;

    @Value("${service.name}")
    private String applicationName;


    private final DarProperties darProperties;

    public DarRequest prepareDarRequest(DarInput darInput) {
        log.info("DarRequestMapper [prepareDarRequest] call for request id {}", darInput.getBankGuaranteeRequestData().getRequestId());
        List<LegalRepresentativeData> selectedSigners = CommonUtils.getSelectedSigner(darInput.getBankGuaranteeRequestData().getLegalRepresentatives());
        return DarRequest.builder()
                .businessLine(darProperties.getBusinessLine())
                .segment(BUSINESS)
                .actorIdType(UUID)
                .userFallbackLanguage(EN.name())
                .userSupportedLanguages(List.of(NL.name(),
                        FR.name(),
                        EN.name(),
                        DE.name()))
                .clientIdentificationRequest(ClientIdentificationRequest.builder()
                        .countryCode(BE.name())
                        .tribe(darProperties.getBusinessLine())
                        .serviceName(applicationName)
                        .build())
                .callback(CallbackRequest.builder()
                        .topic(darProperties.getDocumentSignCallback())
                        .triggerStatus(Set.of(CallbackRequest.TriggerStatusEnum.values()))
                        .build())
                .expirationDate(LocalDate.now().plusDays(darProperties.getDaysToAdd()))
                .agreementId(darInput.getBankGuaranteeRequestData().getRequestId())
                .agreementIdReplacement(TranslationCodeRequest.builder()
                        .translationCode(6)
                        .build())
                .notifications(mapNotification(selectedSigners))
                .productType(TranslationCodeRequest.builder()
                        .translationCode(1)
                        .build())
                .translations(mapTranslations(darInput.getBankGuaranteeRequestData().getReferenceNumber()))
                .frontEndCallback(getTranslationForFrontendCallback(selectedSigners))
                .documentActions(mapDocumentActions(darInput, selectedSigners))
                .build();
    }

    private List<TranslationRequest> mapTranslations(String referenceId) {
        List<TranslationRequest> translationPayloadList = new ArrayList<>();
        Translations translations = TranslationsLoader.loadTranslations();
        translationPayloadList.add(translations.getBankGuarantee());
        translationPayloadList.add(translations.getFilledInConcept());
        translationPayloadList.add(translations.getContract());
        translationPayloadList.add(translations.getSignatoryNotice());
        translationPayloadList.add(translations.getFrontendCallback());
        translationPayloadList.add(getAgreementIdReplacerTranslation(referenceId));
        return translationPayloadList;
    }

    private TranslationRequest getAgreementIdReplacerTranslation(String referenceId) {

        Translations translations = TranslationsLoader.loadTranslations();
        TranslationRequest agreementIdReplacer = translations.getAgreementIdReplacer();
        agreementIdReplacer.getTexts().forEach((key, value) -> agreementIdReplacer.getTexts().put(key, referenceId));
        return agreementIdReplacer;
    }

    private List<DocumentActionRequest> mapDocumentActions(DarInput darInput, List<LegalRepresentativeData> selectedSigners) {
        List<DocumentActionRequest> documentActions = new ArrayList<>();
        int docSuffix = 1;
        for (Document document : darInput.getDocuments()) {
            for (LegalRepresentativeData legalRepresentativeData : selectedSigners) {
                documentActions.add(mapDocumentForSignatory(docSuffix, document,
                        darInput.getBankGuaranteeRequestData(), legalRepresentativeData.getUuid()));
                docSuffix++;
            }
        }
        return documentActions;
    }

    private DocumentActionRequest mapDocumentForSignatory(Integer docSuffix, Document document,
                                                          BankGuaranteeRequestData bankGuaranteeRequestData, String legalRepId) {
        return DocumentActionRequest.builder()
                .name(String.format(darProperties.getSignNamePrefix(), docSuffix))
                .type(SIGN)
                .main(true)
                .signingMean(signingMean)
                .actorId(legalRepId)
                .documentUri(String.format(darProperties.getDocumentUriPrefix(), document.getDocumentId()))
                .documentName(getDocumentNameTranslation(document.getDocumentType()))
                .documentLanguage(bankGuaranteeRequestData.getTranslationLanguage().getLanguage())
                .mandatory(true)
                .signatoryNotice(getSignatoryTranslation())
                .signatoryType(CHECKBOX)
                .build();
    }

    private TranslationCodeRequest getSignatoryTranslation() {
        return TranslationCodeRequest.builder()
                .translationCode(4)
                .build();
    }


    private NotificationRequest mapNotification(List<LegalRepresentativeData> signers) {
        return NotificationRequest.builder()
                .customerNotifications(CustomerNotificationChannelRequest.builder()
                        .email(getEmailAddresses(signers))
                        .build())
                .build();

    }

    private TranslationCodeRequest getDocumentNameTranslation(DocumentType documentType) {
        return TranslationCodeRequest.builder()
                .translationCode(documentType == BG_DRAFT ? 2 : 3)
                .build();
    }

    private FrontEndCallbackRequest getTranslationForFrontendCallback(List<LegalRepresentativeData> legalRepresentatives) {
        return FrontEndCallbackRequest.builder()
                .redirectUrl(darProperties.getFrontendCallbackUrl())
                .cancelUrl(darProperties.getCancelUrl())
                .closeUrl(darProperties.getCloseUrl())
                .redirectText(TranslationCodeRequest.builder()
                        .translationCode(5)
                        .build())
                .exemptActorIds(legalRepresentatives.size() > 1
                        ? legalRepresentatives.stream()
                        .filter(LegalRepresentativeData::isFirstSigner)
                        .findFirst()
                        .map(LegalRepresentativeData::getUuid)
                        .map(List::of)
                        .orElse(List.of())
                        : List.of())
                .build();
    }

    private List<EmailNotificationPayloadContentRequest> getEmailAddresses(List<LegalRepresentativeData> legalReps) {
        List<EmailNotificationPayloadContentRequest> legalRepsEmails = new ArrayList<>();
        for (LegalRepresentativeData legalRepDetail : legalReps) {
            if (StringUtils.isNotEmpty(legalRepDetail.getEmailId())) {
                legalRepsEmails.add(EmailNotificationPayloadContentRequest.builder()
                        .emailAddress(legalRepDetail.getEmailId())
                        .actorId(legalRepDetail.getUuid())
                        .triggerStatus(Set.of(EmailNotificationPayloadContentRequest.TriggerStatusEnum.values()))
                        .communicationLanguage(EN.name())
                        .notificationDates(getReminderDates())
                        .build());
            }
        }
        return legalRepsEmails;
    }

    private List<LocalDateTime> getReminderDates() {
        List<LocalDateTime> reminderDateTimeList = new ArrayList<>();
        for (int i = 1; i < darProperties.getDaysToAdd() - 1; i++) {
            reminderDateTimeList.add(LocalDateTime.now().plusDays(i));
        }
        return reminderDateTimeList;
    }
}