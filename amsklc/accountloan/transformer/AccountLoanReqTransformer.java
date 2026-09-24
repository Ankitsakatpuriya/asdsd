package com.ing.bankguarantees.remote.rest.amsklc.accountloan.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.ing.bankguarantees.utils.ConstantUtils.HEADER_RCID;

/**
 * Class to transform the request, build http client and return {@link Request}
 */
@Slf4j
@Component
public class AccountLoanReqTransformer extends RequestTransformer<String> {

    private static final String CONTRACT_NUMBER = "contractNumber";
    private static final String ING_SOURCE = "X-ING-SOURCE";
    private static final String BG_BE = "BG-BE";
    private final String applicationName;

    public AccountLoanReqTransformer(@Value("${rest.account-loan-api-url}") String urlFormat,
                                     @Value("${bgos.service-name}") String applicationName) {
        super(urlFormat);
        this.applicationName=applicationName;
    }

    @Override
    public Request transform(String contractNumber) {

        Request request = new RichHttpRequestBuilder()
                .withMethod(Method.Get())
                .withUrl(getUrlFormat())
                .withParam(CONTRACT_NUMBER, contractNumber)
                .withHeader(HEADER_RCID, BG_BE)
                .withHeader(ING_SOURCE, applicationName)
                .build();
        log.info(C3LogMarker.marker, "Account Loan API: Calling Account loan API endpoint[{}] with contractNumber  {}",
                request.hashCode(), contractNumber);
        return request;
    }
}
