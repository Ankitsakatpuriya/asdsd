package com.ing.bankguarantees.remote.rest.accountbalance.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceRequest;
import com.ing.bankguarantees.utils.ConstantUtils;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Class to transform the request, build http client and return {@link Request}
 */
@Slf4j
@Component
public class AccountBalanceReqTransformer extends RequestTransformer<AccountBalanceInput> {

    private String currency;

    public AccountBalanceReqTransformer(@Value("${rest.accountbalance-api-account-balance-Search-url}") String urlFormat,
                                        @Value("${bgos.account-balance.default-currency-code}") String defaultCurrency) {
        super(urlFormat);
        this.currency = defaultCurrency;
    }

    @Override
    public Request transform(AccountBalanceInput accountBalanceInput) {

        try {
            AccountBalanceRequest accountBalanceReq = prepareAccountBalanceRequest(accountBalanceInput);
            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(List.of(accountBalanceReq))
                    .withHeader(ConstantUtils.HEADER_RCID, "BG-BE")
                    .build();
            log.info(C3LogMarker.marker, "Account Balance API: Calling Account Balance API endpoint[{}] with requestPayload {}",
                    request.hashCode(), JsonUtils.getJsonFromObject(accountBalanceReq));
            return request;
        } catch (RichHttpRequestBuilderException exception) {
            log.error("Error while parsing the request to call AccountBalanceAPI  {}", exception.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, exception);
        }
    }

    private AccountBalanceRequest prepareAccountBalanceRequest(AccountBalanceInput accountBalanceInput) {
        return AccountBalanceRequest.builder()
                .accountCurrency(currency)
                .ibanNumber(accountBalanceInput.getIbanNumber())
                .accountProductId("1000")
                .build();
    }
}
