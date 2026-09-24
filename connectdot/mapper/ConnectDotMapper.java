package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BaseDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest.Channel;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest.Identifiers;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest.MetaData;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest.Recipient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.*;
import static com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotUtils.unsupportedOperation;
import static com.ing.bankguarantees.utils.CommonUtils.mapLanguage;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectDotMapper {

    public static final String CONTRACT_TEMPLATE = "A-BLNBC01";
    private final BankGuaranteeDocumentMapper bankGuaranteeDocumentMapper;
    private final ContractDocumentMapper contractDocumentMapper;
    private final ConnectDotProperties connectDotProperties;


    public ConnectDotRequest<?> prepareConnectDotRequest(ConnectDotInput connectDotInput) {

        log.info("ConnectDotMapper [prepareConnectDotRequest] Preparing Connect Dot Create Document Payload for" +
                " Doc type {}", connectDotInput.getDocumentType());
        BankGuaranteeCode bgCode = connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode();
        return switch (connectDotInput.getDocumentType()) {
            case BG_DRAFT, BG_FINAL -> mapToBankGuaranteePayload(connectDotInput);
            case CONTRACT, CONTRACT_FINAL -> mapToContractPayload(connectDotInput);
            case CUSTOMIZED_DOC -> unsupportedOperation(bgCode);
        };
    }

    private ConnectDotRequest<?> mapToContractPayload(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [mapToContractPayload] call");
        return buildConnectDotRequest(connectDotInput,
                contractDocumentMapper.mapToContractPayload(connectDotInput));

    }

    private ConnectDotRequest<?> mapToBankGuaranteePayload(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [mapToBankGuaranteePayload] call");
        return buildConnectDotRequest(connectDotInput,
                bankGuaranteeDocumentMapper.mapToBankGuaranteePayload(connectDotInput));

    }


    private <T extends BaseDocumentPayload> ConnectDotRequest<T> buildConnectDotRequest(ConnectDotInput connectDotInput, T payload) {
        log.info("ConnectDotMapper [buildConnectDotRequest] call");
        return ConnectDotRequest.<T>builder()
                .identifiers(prepareIdentifier())
                .metaData(prepareMetadata())
                .recipient(prepareRecipient(connectDotInput))
                .channels(prepareChannels(connectDotInput))
                .payload(payload)
                .build();

    }

    private Identifiers prepareIdentifier() {
        log.info("ConnectDotMapper [prepareIdentifier] call");
        return Identifiers.builder()
                .requestId(UUID.randomUUID())
                .build();
    }

    private MetaData prepareMetadata() {
        log.info("ConnectDotMapper [prepareMetadata] call");
        return MetaData.builder()
                .costCenter(connectDotProperties.getCostCenter())
                .entityCode(connectDotProperties.getEntityCode())
                .initiatingCI(connectDotProperties.getInitiatingCI())
                .requestDate(String.valueOf(ZonedDateTime.now()))
                .communicationDate(String.valueOf(ZonedDateTime.now()))
                .build();
    }

    private Recipient prepareRecipient(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [prepareRecipient] call");
        return Recipient.builder()
                .preferredLanguage(getPreferredLanguage(connectDotInput))
                .partyType(connectDotProperties.getRecipientPartyType())
                .type(connectDotProperties.getRecipientType())
                .build();
    }

    private List<Channel> prepareChannels(ConnectDotInput connectDotInput) {
        log.info("ConnectDotMapper [prepareChannels] call");
        return List.of(Channel.builder()
                .destinations(Set.of(
                        ConnectDotRequest.Destination.builder()
                                .templateName(getTemplateName(connectDotInput))
                                .documentId(connectDotInput.getDocumentId())
                                .output(getDestinationOutput(connectDotInput))
                                .type(connectDotProperties.getDestinationType())
                                .build()))
                .importance(connectDotProperties.getChannelsImportance())
                .build());
    }

    private String getDestinationOutput(ConnectDotInput connectDotInput) {
        BankGuaranteeRequestData bankGuaranteeRequestData = connectDotInput.getBankGuaranteeRequestData();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        boolean stp = connectDotInput.getBankGuaranteeRequestData().isStp();
        return switch (connectDotInput.getDocumentType()) {
            case BG_DRAFT, CONTRACT -> guaranteeDetails.getBgCode() == CUSTOMIZED_TEXT
                    ? connectDotProperties.getDestinationOutputFinal()
                    : connectDotProperties.getDestinationOutput();
            case BG_FINAL, CONTRACT_FINAL -> stp
                    ? connectDotProperties.getDestinationOutput()
                    : connectDotProperties.getDestinationOutputFinal();
            case CUSTOMIZED_DOC ->
                    unsupportedOperation(connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode());
        };
    }

    private String getTemplateName(ConnectDotInput connectDotInput) {
        return switch (connectDotInput.getDocumentType()) {

            case CONTRACT, CONTRACT_FINAL -> CONTRACT_TEMPLATE;
            case BG_DRAFT, BG_FINAL ->
                    connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode().getTemplateName();
            case CUSTOMIZED_DOC ->
                    unsupportedOperation(connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode());
        };
    }

    private String getPreferredLanguage(ConnectDotInput connectDotInput) {

        return switch (connectDotInput.getDocumentType()) {
            case BG_DRAFT, BG_FINAL ->
                    mapLanguage(connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgLanguage().getLanguageCode());
            case CONTRACT, CONTRACT_FINAL ->
                    connectDotInput.getBankGuaranteeRequestData().getTranslationLanguage().toLanguageTag();
            case CUSTOMIZED_DOC ->
                    unsupportedOperation(connectDotInput.getBankGuaranteeRequestData().getGuaranteeDetails().getBgCode());
        };
    }

}

