package com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.KfcCreditOverviewProperties;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.request.CustomerArrangementRequest;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.request.CustomerArrangementRequest.PartyIdentifiersRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.ing.bankguarantees.utils.TracingHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ing.bankguarantees.utils.ConstantUtils.HEADER_RCID;

@Slf4j
@Component
public class CustomerArrangementReqTransformer extends RequestTransformer<String> {

    private static final String HEADER_SOURCE = "X-ING-SOURCE";

    private final KfcCreditOverviewProperties kfcCreditOverviewProperties;
    private final String applicationName;

    public CustomerArrangementReqTransformer(@Value("${rest.customer-arrangement-detail.url}") String urlFormat,
                                             @Value("${bgos.service-name}") String applicationName,
                                             KfcCreditOverviewProperties kfcCreditOverviewProperties) {
        super(urlFormat);
        this.applicationName = applicationName;
        this.kfcCreditOverviewProperties = kfcCreditOverviewProperties;
    }

    @Override
    public Request transform(String accountNumber) {
        try {
            CustomerArrangementRequest customerArrangementRequest = prepareCustomerArrangementRequest(accountNumber);

            Request request = new RichHttpRequestBuilder()
                    .withUrl(String.format(getUrlFormat()))
                    .withMethod(Method.Post())
                    .withJsonContent(customerArrangementRequest)
                    .withHeader(HEADER_RCID, TracingHelper.getINGSpanContext().getTraceId())
                    .withHeader(HEADER_SOURCE, applicationName)
                    .build();

            log.info(C3LogMarker.marker, """
                    KFCCustomerCreditOverview API: Calling POST {} endpoint[{}] to fetch customer arrangement details with data {},
                    """, getUrlFormat(), request.hashCode(), JsonUtils.getJsonFromObject(customerArrangementRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to fetch customer arrangement details {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private CustomerArrangementRequest prepareCustomerArrangementRequest(String accountNumber) {
        return CustomerArrangementRequest.builder()
                .involvedPartyIdentifiers(List.of(PartyIdentifiersRequest.builder()
                        .type(kfcCreditOverviewProperties.getInvolvePartyType())
                        .value(accountNumber)
                        .build()))
                .employeeId(CustomerArrangementRequest.EmployeeIdRequest.builder()
                        .type(kfcCreditOverviewProperties.getEmployeeIdType())
                        .value(kfcCreditOverviewProperties.getEmployeeIdValue())
                        .build())
                .level(kfcCreditOverviewProperties.getLevel())
                .build();
    }
}