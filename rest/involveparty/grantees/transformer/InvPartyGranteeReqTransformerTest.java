package com.ing.bankguarantees.remote.rest.involveparty.grantees.transformer;

import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvPartyGranteeReqTransformerTest {
    private static final String URL_FORMAT = "https://apis.ing.com/v5/involved-parties/{uuid}/grantees";
    private static final String UUID = "0325895254";
    private static final String OFFSET_VALUE = "0";
    private static final String LIMIT_VALUE = "1000";


    @Test
    void transformTest() {
        Request request = new InvPartyGranteeReqTransformer(URL_FORMAT).transform(UUID);
        assertThat(request.uri()).isEqualTo(String.format("/v5/involved-parties/%s/grantees?offset=%s&limit=%s", UUID, OFFSET_VALUE, LIMIT_VALUE));
        assertThat(request.method()).isEqualTo(Method.Get());
    }
}
