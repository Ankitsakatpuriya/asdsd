package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer;

import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ExtIdentReqTransformerTest {
    private static final String URL_FORMAT = "https://apis.ing.com/v5/involved-parties/{uuid}/external-identifiers";
    private static final String UUID = "0325895254";

    @Test
    void transformTest() {
        Request request = new ExtIdentReqTransformer(URL_FORMAT).transform(UUID);
        assertThat(request.uri()).isEqualTo(String.format("/v5/involved-parties/%s/external-identifiers", UUID));
        assertThat(request.method()).isEqualTo(Method.Get());
    }

}
