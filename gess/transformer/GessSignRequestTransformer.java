package com.ing.bankguarantees.remote.rest.gess.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.enums.BankGuaranteeLanguage;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotUtils;
import com.ing.bankguarantees.remote.rest.gess.GessProperties;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignInput;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignRequest;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignRequest.GessSignConfigRequest;
import com.ing.bankguarantees.remote.rest.gess.model.request.GessSignRequest.GessSignMetadataRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;
import static com.ing.bankguarantees.remote.rest.connectdot.mapper.ConnectDotUtils.unsupportedOperation;

@Slf4j
@Component
public class GessSignRequestTransformer extends RequestTransformer<GessSignInput> {

    private static final String REQUESTER_EMAIL = "requester-email";
    private static final String REQUESTER_ID = "requester-id";
    private static final String REQUESTER_SYSTEM_ID = "requester-systemid";
    private static final String FIRST_SIGNER_KEY = "first";
    private static final String SECOND_SIGNER_KEY = "second";
    private final GessProperties gessProperties;

    @Value("${bgos.ing.signer.config.first.certificate-id}")
    private String firstSignerCertificateId;

    @Value("${bgos.ing.signer.config.second.certificate-id}")
    private String secondSignerCertificateId;

    @Value("${bgos.ing.signer.config.first.pin}")
    private String firstSignerPin;

    @Value("${bgos.ing.signer.config.second.pin}")
    private String secondSignerPin;


    public GessSignRequestTransformer(@Value("${rest.gess.pkbox.esign.url}") String urlFormat,
                                      GessProperties gessProperties) {
        super(urlFormat);
        this.gessProperties = gessProperties;
    }

    @Override
    public Request transform(GessSignInput gessSignInput) {

        try {
            GessSignRequest gessSignRequest = prepareGessSignRequest(gessSignInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withHeader(REQUESTER_EMAIL, gessSignInput.requesterEmail())
                    .withHeader(REQUESTER_ID, gessSignInput.requesterId().toString())
                    .withHeader(REQUESTER_SYSTEM_ID, gessSignInput.requesterSystemId())
                    .withJsonContent(gessSignRequest)
                    .build();
            log.info(C3LogMarker.marker, "Gess API: Calling POST {} endpoint[{}] for file name {}, with Request {}", getUrlFormat(), request.hashCode(), gessSignInput.fileName(),
                    JsonUtils.getJsonFromObject(gessSignRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call Gess Sign API {}", ex.getMessage());
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }

    private GessSignRequest prepareGessSignRequest(GessSignInput gessSignInput) {
        return GessSignRequest.builder()
                .document(GessSignRequest.GessDocRequest.builder()
                        .base64Content(gessSignInput.documentBase64())
                        .fileNameWithExtension(gessSignInput.fileName())
                        .build())
                .signatures(List.of(GessSignMetadataRequest.builder()
                                .certificateId(firstSignerCertificateId)
                                .pin(firstSignerPin)
                                .config(prepareConfig(gessSignInput, FIRST_SIGNER_KEY))
                                .build(),
                        GessSignMetadataRequest.builder()
                                .certificateId(secondSignerCertificateId)
                                .pin(secondSignerPin)
                                .config(prepareConfig(gessSignInput, SECOND_SIGNER_KEY))
                                .build()))
                .build();

    }

    private GessSignConfigRequest prepareConfig(GessSignInput gessSignInput, String signerKey) {
        Optional<List<Integer>> optionalConfig = getSignaturePosition(gessSignInput, signerKey);
        return GessSignConfigRequest.builder()
                .width(optionalConfig.map(list -> list.get(2) > 0 ? list.get(2) : null).orElse(null))
                .height(optionalConfig.map(list -> list.get(3) > 0 ? list.get(3) : null).orElse(null))
                .xPosition(optionalConfig.map(list -> list.get(0) > 0 ? list.get(0) : null).orElse(null))
                .yPosition(optionalConfig.map(list -> list.get(1) > 0 ? list.get(1) : null).orElse(null))
                .pageNumber(optionalConfig.map(list -> list.get(4) > 0 ? list.get(4) : null).orElse(null))
                .build();
    }

    ;

    private Optional<List<Integer>> getSignaturePosition(GessSignInput gessSignInput, String signerKey) {

        return switch (gessSignInput.documentType()) {
            case CONTRACT, CONTRACT_FINAL ->
                    Optional.of(gessProperties.getCoordinateConfig().getContractLetter().get(signerKey));
            case BG_DRAFT, BG_FINAL -> switch (gessSignInput.bankGuaranteeCode()) {
                case PUBLIC_CONTRACT ->
                        Optional.of(gessProperties.getCoordinateConfig().getPublicContract().get(signerKey));
                case PERFORMANCE_BOND, ADVANCE_PAYMENT, PAYMENT_GUARANTEE, MONEY_RETENTION_BOND, BID_BOND ->
                        Optional.of(gessProperties.getCoordinateConfig().getAbstractModel().get(signerKey));
                case RENTAL -> Optional.of(gessProperties.getCoordinateConfig().getRental().get(signerKey));
                case REAL_ESTATE -> Optional.of(gessProperties.getCoordinateConfig().getRealEstate().get(signerKey));
                case STATE_LOTTERY ->
                        Optional.of(gessProperties.getCoordinateConfig().getStateLottery().get(signerKey));
                case CUSTOM_1 -> Optional.of(gessProperties.getCoordinateConfig().getCustomOne().get(signerKey));
                case CUSTOM_2 -> Optional.of(gessProperties.getCoordinateConfig().getCustomTwo().get(signerKey));
                case CUSTOM_4 -> gessSignInput.bankGuaranteeLanguage() == BankGuaranteeLanguage.FRENCH
                        ? Optional.of(gessProperties.getCoordinateConfig().getCustomFourFrench().get(signerKey))
                        : Optional.of(gessProperties.getCoordinateConfig().getCustomFourDutch().get(signerKey));
                case CUSTOM_5 -> Optional.of(gessProperties.getCoordinateConfig().getCustomFive().get(signerKey));
                case OVAM -> Optional.of(gessProperties.getCoordinateConfig().getOvam().get(signerKey));
                case WOODS_BG_DISCHARGE, WOODS_BG_PRIVATE, WOODS_PROM_A, WOODS_BGVLA_PUBLIC, WOODS_PROM_VLA,
                     WOODS_BGWAL_PUBLIC, WOODS_PROM_B, ABSTRACT_PROM, PUBLIC_CONTRACT_PROM ->
                        Optional.of(gessProperties.getCoordinateConfig().getWoods().get(signerKey));
                default -> Optional.empty();
            };
            case CUSTOMIZED_DOC -> unsupportedOperation(gessSignInput.bankGuaranteeCode());
        };
    }
}