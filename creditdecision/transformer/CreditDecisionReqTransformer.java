package com.ing.bankguarantees.remote.rest.creditdecision.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.error.logger.ExceptionLogger;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.creditdecision.CreditDecisionProperties;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditRiskRequest;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ing.bankguarantees.utils.JsonUtils.getJsonFromObject;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.leftPad;

/**
 * Transformer for the request - to get scoring from the Credit Risk API
 */
@Slf4j
@Component
public class CreditDecisionReqTransformer extends RequestTransformer<CreditDecisionInput> {

    private static final String AMSTERDAM_ZONE_ID = "Europe/Amsterdam";
    private static final String HEADER_REQ_ID = "X-ING-REQ-ID";
    private static final String HEADER_SOURCE = "X-ING-SOURCE";

    private final CreditDecisionProperties creditDecisionProperties;
    private final String applicationName;

    public CreditDecisionReqTransformer(@Value("${rest.credit-risk-api-credit-decision-url}") String urlFormat,
                                        @Value("${bgos.service-name}") String applicationName, CreditDecisionProperties creditDecisionProperties) {
        super(urlFormat);
        this.applicationName = applicationName;
        this.creditDecisionProperties = creditDecisionProperties;
    }

    @Override
    public Request transform(CreditDecisionInput input) {
        log.info("CreditRiskReqTransformer [transform] Call ");
        try {
            CreditRiskRequest creditRiskRequest = prepareCreditRiskRequest(input);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withHeader(HEADER_REQ_ID, UUID.randomUUID().toString())
                    .withHeader(HEADER_SOURCE, applicationName)
                    .withJsonContent(creditRiskRequest)
                    .build();
            log.info(C3LogMarker.marker, "CreditRiskAPI: Calling POST {} endpoint[{}] X-ING-REQ-ID [{}] with payload {}",
                    getUrlFormat(), request.hashCode(),
                    request.headerMap().get(HEADER_REQ_ID).get(), getJsonFromObject(creditRiskRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            ExceptionLogger.error(ex, "CreditRiskAPI: Error when trying to create request payload with exception {}", ex.getMessage());
            throw new BgosException(ErrorCode.INVALID_REQUEST, ex);
        }
    }

    private CreditRiskRequest prepareCreditRiskRequest(CreditDecisionInput input) {
        log.info("CreditRiskReqTransformer [prepareCreditRiskRequest] Call ");
        return CreditRiskRequest.builder()
                .requestId(input.requestId())
                .requestDate(BASIC_ISO_DATE.format(LocalDate.now(ZoneId.of(AMSTERDAM_ZONE_ID))))
                .requestChannel(creditDecisionProperties.getRequestChannel())
                .productType(creditDecisionProperties.getProductType())
                .requestType(creditDecisionProperties.getRequestType())
                .openingBranch(creditDecisionProperties.getDefaultBranch())
                .followupBranch(creditDecisionProperties.getDefaultBranch())
                .professionalUseFlag(true)
                .agreementCurrency(creditDecisionProperties.getEurCurrency())
                .creditOperations(singletonList(prepareCreditOperation(input)))
                .involvedparties(prepareInvolvePartyList(prepareIndividualInvolvedParty(input).orElse(null),
                        prepareOrgInvolvedParty(input).orElse(null)))
                .build();
    }

    private List<CreditRiskRequest.InvolvedParty> prepareInvolvePartyList(CreditRiskRequest.InvolvedParty individualInvolvedParty,
                                                                          CreditRiskRequest.InvolvedParty orgInvolvedParty) {
        var involvePartyList = new ArrayList<CreditRiskRequest.InvolvedParty>();
        if (isNotEmpty(individualInvolvedParty)) involvePartyList.add(individualInvolvedParty);
        if (isNotEmpty(orgInvolvedParty)) involvePartyList.add(orgInvolvedParty);
        return involvePartyList;
    }

    private Optional<CreditRiskRequest.InvolvedParty> prepareOrgInvolvedParty(CreditDecisionInput input) {
        log.info("CreditRiskReqTransformer [prepareOrgInvolvedParty] Call ");
        CreditDecisionInput.PamRequestIdentifier orgIdentifier = input.organisationId();
        return isNotEmpty(orgIdentifier)
                ? Optional.of(CreditRiskRequest.InvolvedParty.builder()
                .involvedPartyInternalIdentifier(CreditRiskRequest.InvolvedPartyInternalIdentifier.builder()
                        .id(csiIdentifier(orgIdentifier.getValue()))
                        .type(orgIdentifier.getType())
                        .build())
                .workStability(creditDecisionProperties.getLegalEntityWorkStability())
                .partnershipType(creditDecisionProperties.getOrganisationPartnershipType())
                .intervenientType(creditDecisionProperties.getOrganisationIntervenientType())
                .build())
                : Optional.empty();

    }

    private Optional<CreditRiskRequest.InvolvedParty> prepareIndividualInvolvedParty(CreditDecisionInput input) {

        log.info("CreditRiskReqTransformer [prepareIndividualInvolvedParty] Call ");
        CreditDecisionInput.PamRequestIdentifier indIdentifier = input.individualId();
        return isNotEmpty(indIdentifier)
                ? Optional.of(CreditRiskRequest.InvolvedParty.builder()
                .involvedPartyInternalIdentifier(CreditRiskRequest.InvolvedPartyInternalIdentifier.builder()
                        .id(csiIdentifier(indIdentifier.getValue()))
                        .type(indIdentifier.getType())
                        .build())
                .workStability(creditDecisionProperties.getSelfEmployedWorkStability())
                .partnershipType(creditDecisionProperties.getIndividualPartnershipType())
                .intervenientType(creditDecisionProperties.getIndividualIntervenientType())
                .build())
                : Optional.empty();

    }

    private CreditRiskRequest.CreditOperation prepareCreditOperation(CreditDecisionInput input) {
        log.info("CreditRiskReqTransformer [prepareCreditOperation] Call ");
        return CreditRiskRequest.CreditOperation.builder()
                .productNatureType(creditDecisionProperties.getProductNatureType())
                .creditAmount(input.amount())
                .privateUse(creditDecisionProperties.getPrivateUse())
                .capitalPaymentFrequency(creditDecisionProperties.getCapitalPaymentFrequency())
                .operationProductType(creditDecisionProperties.getProductType())
                .durationMonths(creditDecisionProperties.getGreenPdlBusinessLineDurationInMonths())
                .build();

    }

    private String csiIdentifier(String input) {
        return input.length() < creditDecisionProperties.getCsiLength()
                ? leftPad(input, creditDecisionProperties.getCsiLength(), "0")
                : input;
    }

}
