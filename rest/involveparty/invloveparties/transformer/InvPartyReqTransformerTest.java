package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.transformer;

import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class InvPartyReqTransformerTest {
    private static final String URL_FORMAT = "https://apis.ing.com/v6/involved-parties/{uuid}";
    private static final String UUID = "0325895254";


    @Test
    void transformTest() {
        Request request = new InvPartyReqTransformer(URL_FORMAT).transform(RequestAdapter.getInvolvePartyRequest(UUID, Boolean.FALSE));
        assertThat(request.uri()).isEqualTo(String.format("/v6/involved-parties/%s", UUID));
        assertThat(request.method()).isEqualTo(Method.Get());
    }
}
