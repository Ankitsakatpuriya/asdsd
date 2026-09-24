package com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.request.BankAccountNumberRequest;
import com.ing.bankguarantees.utils.ConstantUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EqualsAndHashCode(callSuper = false)
public class BankAccountNumberReqTransformer extends RequestTransformer<String> {

    private static final String BANK_ACCOUNT_NUMBER_ACCOUNT_TYPE = "BELOANNUMBER";

    public BankAccountNumberReqTransformer(@Value("${rest.bank-account-number-api-single-get-url}") String urlFormat) {
        super(urlFormat);
    }

    @Override
    public Request transform(String requestId) {
        try {
            BankAccountNumberRequest bankAccountNumberRequest = prepareBankAccountNumberRequest(requestId);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(bankAccountNumberRequest)
                    .withHeader(ConstantUtils.HEADER_RCID, "BG-BE")
                    .build();

            log.info(C3LogMarker.marker, "Calling BankAccountNumber endpoint[{}]  ", request.hashCode());

            return request;
        } catch (RichHttpRequestBuilderException exception) {
            log.error("Error while parsing the request to call BankAccountNumber  {}", exception.getMessage());
            throw new BgosException(ErrorCode.INVALID_REQUEST,exception);
        }
    }

    private BankAccountNumberRequest prepareBankAccountNumberRequest(String requestId) {
        return BankAccountNumberRequest.builder()
                .requestId(requestId)
                .accountType(BANK_ACCOUNT_NUMBER_ACCOUNT_TYPE)
                .build();
    }
}
