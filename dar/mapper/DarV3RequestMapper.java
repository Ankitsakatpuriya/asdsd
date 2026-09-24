package com.ing.bankguarantees.remote.rest.dar.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.dar.DarProperties;
import com.ing.bankguarantees.remote.rest.dar.model.request.*;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarV3Request.*;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarV3Request.FrontEndCallbackRequest;
import com.ing.bankguarantees.remote.rest.dar.model.request.DarV3Request.NotificationRequest;
import com.ing.bankguarantees.remote.utils.TranslationsLoader;
import com.ing.bankguarantees.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ing.bankguarantees.models.enums.DocumentType.BG_DRAFT;
import static com.ing.bankguarantees.remote.rest.dar.model.request.CountryCodes.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class DarV3RequestMapper {

    private final static String FIRST_SIGNER_CODE = "BG_SIGNER_01";
    private final static String SECOND_SIGNER_CODE = "BG_SIGNER_02";

    private final static String FIRST_FE_REDIRECT_CODE = "BG_REDIRECT_01";
    private final static String SECOND_FE_REDIRECT_CODE = "BG_REDIRECT_02";

    private final static String FIRST_DOC_CODE = "BG_DOC_01";
    private final static String SECOND_DOC_CODE = "BG_DOC_02";

    private final static String FIRST_ACTION_CODE = "BG_ACTION_01";
    private final static String SECOND_ACTION_CODE = "BG_ACTION_02";


    @Value("${bgos.docsign-signingmean}")
    private String signingMean;

    @Value("${bgos.service-activity-type}")
    private String serviceActivity;

    @Value("${service.name}")
    private String applicationName;

    private final DarProperties darProperties;

    public DarV3Request prepareDarV3Request(DarInput darInput) {
        BankGuaranteeRequestData bgRequest = darInput.getBankGuaranteeRequestData();
        List<LegalRepresentativeData> selectedSigner = CommonUtils.getSelectedSigner(darInput.getBankGuaranteeRequestData().getLegalRepresentatives());
        int signerCount = CollectionUtils.size(selectedSigner);
        return DarV3Request.builder()
                .product(prepareProductRequest())
                .signers(prepareSigners(selectedSigner))
                .general(prepareGeneralRequest(darInput))
                .documents(prepareDocumentRequest(darInput))
                .clientCallback(prepareClientCallbackRequest(signerCount))
                .notification(prepareNotifications(bgRequest.getSignExpiryDate(), bgRequest.getTranslationLanguage().getLanguage().toUpperCase(), selectedSigner))
                .frontEndCallbacks(prepareFrontEndCallback(signerCount))
                .translations(prepareTranslations(darInput.getBankGuaranteeRequestData().getReferenceNumber()))
                .actions(prepareActions(CollectionUtils.size(darInput.getDocuments()), signerCount))
                .build();
    }


    private GeneralRequest prepareGeneralRequest(DarInput darInput) {
        return GeneralRequest.builder()
                .referenceId(darInput.getBankGuaranteeRequestData().getRequestId())
                .expiryDate(darInput.getBankGuaranteeRequestData().getSignExpiryDate())
                .userFallbackLanguage(CountryCodes.EN.name())
                .userSupportedLanguages(List.of(NL.name(),
                        FR.name(),
                        EN.name(),
                        DE.name()))
                .referenceReplacementText(TranslationCodeV3Request.builder()
                        .translationCode(6)
                        .build())
                .build();
    }

    private ProductRequest prepareProductRequest() {
        return ProductRequest.builder()
                .tribe(darProperties.getTribe())
                .segment(Segment.BUSINESS)
                .countryCode(BE.name())
                .serviceActivity(serviceActivity)
                .serviceName(applicationName)
                .businessLine(darProperties.getBusinessLine())
                .productType(TranslationCodeV3Request.builder()
                        .translationCode(1)
                        .build())
                .build();
    }

    private ClientCallbackRequest prepareClientCallbackRequest(Integer signerCount) {
        return ClientCallbackRequest.builder()
                .topic(signerCount > 1 ? darProperties.getDocumentSignIntermediateCallback()
                        : darProperties.getDocumentSignCallback())
                .triggerStatus(TriggerStatusV3.getCallbackStatus())
                .detailedCallbackTopic(signerCount > 1)
                .intermediateCallbacks(prepareIntermediateCallback(signerCount).orElse(null))
                .build();
    }

    private Optional<List<IntermediateCallbackRequest>> prepareIntermediateCallback(Integer signerCount) {
        return signerCount > 1 ? Optional.of(List.of(IntermediateCallbackRequest.builder()
                .triggerIntermediateStatus(Set.of(IntermediateStatus.SIGNER_DONE))
                .signerCode(FIRST_SIGNER_CODE)
                .build())) : Optional.empty();
    }

    private List<SignerRequest> prepareSigners(List<LegalRepresentativeData> selectedSigner) {

        List<SignerRequest> signers = new ArrayList<>();
        for (LegalRepresentativeData legalRepresentativeData : selectedSigner) {
            signers.add(SignerRequest.builder()
                    .code(getSignerCode(legalRepresentativeData))
                    .language(legalRepresentativeData.getPreferredLanguage())
                    .idType(ActorIdType.UUID)
                    .idNumber(legalRepresentativeData.getUuid())
                    .type(SignerType.ING_CUSTOMER)
                    .emailAddress(legalRepresentativeData.getEmailId())
                    .frontEndCallbackCode(getFrontEndCallbackCode(legalRepresentativeData.isFirstSigner(), CollectionUtils.size(selectedSigner)))
                    .build());
        }
        return signers;
    }

    private List<DocumentRequest> prepareDocumentRequest(DarInput darInput) {
        List<DocumentRequest> documents = new ArrayList<>();
        for (int i = 0; i < darInput.getDocuments().size(); i++) {
            Document document = darInput.getDocuments().get(i);
            documents.add(DocumentRequest.builder()
                    .code(i == 0 ? FIRST_DOC_CODE : SECOND_DOC_CODE)
                    .name(getDocumentNameTranslation(document.getDocumentType()))
                    .link(String.format(darProperties.getDocumentUriPrefix(), document.getDocumentId()))
                    .language(getDocumentLanguage(darInput.getBankGuaranteeRequestData(), document.getDocumentType()))
                    .build());
        }
        return documents;
    }

    private List<ActionRequest> prepareActions(Integer documentsCount, Integer signerCount) {

        List<ActionRequest> actions = new ArrayList<>();
        for (int i = 0; i < documentsCount; i++) {
            actions.add(ActionRequest.builder()
                    .code(i == 0 ? FIRST_ACTION_CODE : SECOND_ACTION_CODE)
                    .main(true)
                    .mandatory(true)
                    .type(DocumentActionType.SIGN)
                    .signing(SignActionRequest.builder()
                            .signingMean(signingMean)
                            .signatoryNotice(getSignatoryTranslation())
                            .signatoryNoticeType(SignatoryType.CHECKBOX)
                            .build())
                    .signerCodes(signerCount > 1
                            ? List.of(FIRST_SIGNER_CODE, SECOND_SIGNER_CODE)
                            : List.of(FIRST_SIGNER_CODE))
                    .documentCodes(i == 0 ? List.of(FIRST_DOC_CODE) : List.of(SECOND_DOC_CODE))
                    .build());
        }
        return actions;
    }


    private NotificationRequest prepareNotifications(LocalDate expiryDate, String language, List<LegalRepresentativeData> selectedSigner) {

        List<EmailNotificationRequest> emailNotificationRequestList = getGeneralEmailNotificationRequest(language, selectedSigner);
        emailNotificationRequestList.addAll(getExpiryEmailNotificationRequest(language, selectedSigner));
        emailNotificationRequestList.addAll(getReminderEmailNotificationRequest(expiryDate, language, selectedSigner));
        return NotificationRequest.builder()
                .customerNotifications(CustomerNotificationsRequest.builder()
                        .email(emailNotificationRequestList)
                        .build())
                .build();
    }


    private List<EmailNotificationRequest> getGeneralEmailNotificationRequest(String language, List<LegalRepresentativeData> selectedSigner) {
        List<EmailNotificationRequest> emailNotifications = new ArrayList<>();
        for (LegalRepresentativeData legalRepresentativeData : selectedSigner) {
            if (StringUtils.isNotEmpty(legalRepresentativeData.getEmailId())) {
                emailNotifications.add(EmailNotificationRequest.builder()
                        .communicationLanguage(language)
                        .signerCodes(List.of(getSignerCode(legalRepresentativeData)))
                        .triggerStatus(Set.of(TriggerStatusV3.DRAFT,
                                TriggerStatusV3.CANCELLED,
                                TriggerStatusV3.REVOKED,
                                TriggerStatusV3.DONE))
                        .emailAddress(legalRepresentativeData.getEmailId())
                        .build());
            }
        }
        return emailNotifications;
    }

    private List<EmailNotificationRequest> getExpiryEmailNotificationRequest(String language, List<LegalRepresentativeData> selectedSigner) {
        List<EmailNotificationRequest> emailNotifications = new ArrayList<>();
        for (LegalRepresentativeData legalRepresentativeData : selectedSigner) {
            if (StringUtils.isNotEmpty(legalRepresentativeData.getEmailId())) {
                emailNotifications.add(EmailNotificationRequest.builder()
                        .communicationLanguage(language)
                        .businessMessage(getExpiryBusinessMessage(language))
                        .signerCodes(List.of(getSignerCode(legalRepresentativeData)))
                        .triggerStatus(Set.of(TriggerStatusV3.EXPIRED))
                        .emailAddress(legalRepresentativeData.getEmailId())
                        .build());
            }
        }
        return emailNotifications;
    }

    private List<EmailNotificationRequest> getReminderEmailNotificationRequest(LocalDate expiryDate, String language, List<LegalRepresentativeData> selectedSigner) {
        List<EmailNotificationRequest> emailNotifications = new ArrayList<>();
        for (LegalRepresentativeData legalRepresentativeData : selectedSigner) {
            if (StringUtils.isNotEmpty(legalRepresentativeData.getEmailId())) {
                emailNotifications.add(EmailNotificationRequest.builder()
                        .communicationLanguage(language)
                        .signerCodes(List.of(getSignerCode(legalRepresentativeData)))
                        .notificationDates(getReminderDate(expiryDate))
                        .triggerStatus(Set.of(TriggerStatusV3.REMINDER))
                        .emailAddress(legalRepresentativeData.getEmailId())
                        .build());
            }
        }
        return emailNotifications;
    }

    private List<LocalDateTime> getReminderDate(LocalDate expiryDate) {
        List<LocalDateTime> reminderDates = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            reminderDates.add(expiryDate.minusDays(i).atTime(11, 0));
        }
        return reminderDates;

    }

    private String getExpiryBusinessMessage(String language) {
        return switch (language) {
            case "fr" -> darProperties.getExpiryMessageFr();
            case "nl" -> darProperties.getExpiryMessageNl();
            default -> darProperties.getExpiryMessageEn();
        };
    }

    private String getDocumentLanguage(BankGuaranteeRequestData bankGuaranteeRequestData, DocumentType documentType) {
        return switch (documentType) {
            case BG_DRAFT -> bankGuaranteeRequestData.getGuaranteeDetails().getBgLanguage().getLanguageCode();
            case CONTRACT -> bankGuaranteeRequestData.getTranslationLanguage().getLanguage();
            default -> "en";
        };

    }

    private List<FrontEndCallbackRequest> prepareFrontEndCallback(Integer signerCount) {

        List<FrontEndCallbackRequest> frontEndCallbacks = new ArrayList<>();
        for (int i = 0; i < signerCount; i++) {
            String redirectCode = (i == 0 && signerCount > 1) ? FIRST_FE_REDIRECT_CODE : SECOND_FE_REDIRECT_CODE;
            frontEndCallbacks.add(FrontEndCallbackRequest.builder()
                    .code(redirectCode)
                    .redirectUrl(redirectCode.equals(FIRST_FE_REDIRECT_CODE)
                            ? darProperties.getFirstConfirmationLink()
                            : darProperties.getSecondConfirmationLink())
                    .cancelUrl(darProperties.getCancelUrl())
                    .closeUrl(darProperties.getCloseUrl())
                    .redirectText(TranslationCodeV3Request.builder()
                            .translationCode(5)
                            .build())
                    .build());
        }
        return frontEndCallbacks;
    }


    private TranslationCodeV3Request getDocumentNameTranslation(DocumentType documentType) {
        return TranslationCodeV3Request.builder()
                .translationCode(documentType == BG_DRAFT ? 2 : 3)
                .build();
    }

    private TranslationCodeV3Request getSignatoryTranslation() {
        return TranslationCodeV3Request.builder()
                .translationCode(4)
                .build();
    }

    private String getSignerCode(LegalRepresentativeData legalRepresentativeData) {
        return legalRepresentativeData.isFirstSigner() ? FIRST_SIGNER_CODE : SECOND_SIGNER_CODE;
    }

    private String getFrontEndCallbackCode(boolean firstSigner, Integer signerCount) {
        return firstSigner && signerCount > 1 ? FIRST_FE_REDIRECT_CODE : SECOND_FE_REDIRECT_CODE;
    }


    private List<TranslationRequest> prepareTranslations(String referenceId) {
        List<TranslationRequest> translationPayloadList = new ArrayList<>();
        Translations translations = TranslationsLoader.loadTranslations();
        translationPayloadList.add(translations.getBankGuarantee());
        translationPayloadList.add(translations.getFilledInConcept());
        translationPayloadList.add(translations.getContract());
        translationPayloadList.add(translations.getSignatoryNotice());
        translationPayloadList.add(translations.getFrontendCallback());
        translationPayloadList.add(getReferenceIdReplacerTranslation(referenceId));
        return translationPayloadList;
    }


    private TranslationRequest getReferenceIdReplacerTranslation(String referenceId) {

        Translations translations = TranslationsLoader.loadTranslations();
        TranslationRequest agreementIdReplacer = translations.getAgreementIdReplacer();
        agreementIdReplacer.getTexts().forEach((key, value) -> agreementIdReplacer.getTexts().put(key, referenceId));
        return agreementIdReplacer;
    }

}