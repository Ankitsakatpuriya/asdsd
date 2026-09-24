package com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditLineBalanceRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.ConstantUtils.HEADER_RCID;

@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class CreditLineReqTransformer extends RequestTransformer<CreditBalanceInput> {

    private final String currency;
    private final String applicationName;
    private static final String HEADER_SOURCE = "X-ING-SOURCE";

    public CreditLineReqTransformer(@Value("${rest.customerlending-api-creditline-url}") String urlFormat,
                                    @Value("${bgos.service-name}") String applicationName,
                                    @Value("${bgos.account-balance.default-currency-code}") String currency) {
        super(urlFormat);
        this.applicationName = applicationName;
        this.currency = currency;
    }

    @Override
    public Request transform(CreditBalanceInput creditLineRequestIn) {
        log.debug("Sending request to CustomerLendingArrangements API to get credit line balance ");
        try {
            CreditLineBalanceRequest creditLineBalanceRequest = prepareCreditLineRequest(creditLineRequestIn);

            Request request = new RichHttpRequestBuilder()
                    .withUrl(String.format(getUrlFormat()))
                    .withMethod(Method.Post())
                    .withJsonContent(creditLineBalanceRequest)
                    .withHeader(HEADER_RCID, "BG-BE")
                    .withHeader(HEADER_SOURCE, applicationName)
                    .build();

            log.info(C3LogMarker.marker, "CustomerLendingArrangements API endpoint endpoint[{}] with requestPayload {}",
                    request.hashCode(), JsonUtils.getJsonFromObject(creditLineBalanceRequest));
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call Party Search api {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private CreditLineBalanceRequest prepareCreditLineRequest(CreditBalanceInput creditLineRequestIn) {
        return CreditLineBalanceRequest.builder()
                .currency(Integer.valueOf(currency))
                .accountNumber(creditLineRequestIn.getCreditLineAccountNumber())
                .productCode(6000)
                .build();
    }
}