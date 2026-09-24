package com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.transformer;

import com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.BankAccountNumberReqTransformer;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BankAccountNumberReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/bankaccountnumbers/single/get";

    @Test
    void transformTest() {

        Request request = new BankAccountNumberReqTransformer(URL_FORMAT)
                .transform("requestId");
        assertThat(request.uri()).isEqualTo("/bankaccountnumbers/single/get");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isEqualTo("{\"requestId\":\"requestId\",\"accountType\":\"BELOANNUMBER\"}");

    }

}