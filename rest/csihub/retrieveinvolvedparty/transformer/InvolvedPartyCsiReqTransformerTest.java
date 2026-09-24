package com.ing.bankguarantees.remote.rest.csihub.retrieveinvolvedparty.transformer;

import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Slf4j
class InvolvedPartyCsiReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/csi/involved-party/retrieve-involved-party-details";
    private static final String CBE_NUMBER = "12345678";

    @Test
    void transformTest() {
        Request request = new InvolvedPartyCsiReqTransformer(URL_FORMAT)
                .transform(RequestAdapter.getCsiCbeRequest(CBE_NUMBER));
        assertThat(request.uri()).isEqualTo("/csi/involved-party/retrieve-involved-party-details");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.getContentString()).isEqualTo("{\"involvedParty\":{\"involvedPartyRegistration\":" +
                "{\"registrationAuthority\":2,\"registrationNumber\":\"12345678\"}}}");
    }


}
