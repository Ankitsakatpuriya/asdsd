package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.CreditLineArrangementProperties;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.mapper.CreditLineArrangementRequestMapper;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementRequest;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static com.ing.bankguarantees.utils.ConstantUtils.HEADER_RCID;

@Slf4j
@Component
public class CreditLineArrangementReqTransformer extends RequestTransformer<CreditLineArrangementRequest> {

    private final String applicationName;
    private static final String HEADER_SOURCE = "X-ING-SOURCE";
    private final CreditLineArrangementProperties creditLineArrangementProperties;
    private final CreditLineArrangementRequestMapper creditLineArrangementRequestMapper;

    public CreditLineArrangementReqTransformer(@Value("${rest.customerlending-api-creditlinearrangement-url}") String urlFormat,
                                               @Value("${bgos.service-name}") String applicationName,
                                               CreditLineArrangementProperties creditLineArrangementProperties,
                                               CreditLineArrangementRequestMapper creditLineArrangementRequestMapper) {
        super(urlFormat);
        this.applicationName = applicationName;
        this.creditLineArrangementProperties = creditLineArrangementProperties;
        this.creditLineArrangementRequestMapper = creditLineArrangementRequestMapper;
    }

    @Override
    public Request transform(CreditLineArrangementRequest creditLineArrangementRequest) {
        log.debug("Sending request to CreditLineArrangement API to get credit line balance ");
        try {

            CreditLineArrangementInput creditLineArrangementInput = creditLineArrangementRequestMapper.prepareCreditLineArrangementInput(creditLineArrangementRequest);
            Request request = new RichHttpRequestBuilder()
                    .withUrl(String.format(getUrlFormat()))
                    .withMethod(Method.Post())
                    .withJsonContent(creditLineArrangementInput)
                    .withHeader(HEADER_RCID, "BG-BE")
                    .withHeader(HEADER_SOURCE, applicationName)
                    .build();

            log.info(C3LogMarker.marker, "CreditLineArrangement API endpoint endpoint[{}] with requestPayload {} and method {}",
                    request.hashCode(), JsonUtils.getJsonFromObject(creditLineArrangementInput), creditLineArrangementRequest.requestType());
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call Party Search api {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }


}
