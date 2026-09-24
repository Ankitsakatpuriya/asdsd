package com.ing.bankguarantees.remote.rest.pamqualification.transformer;

import com.ing.bankguarantees.remote.rest.pamqualification.PamQualificationProperties;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PamQualificationReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/api/qualification-checks/parties";
    private static final String ORGANIZATION_ID = "b6c4e28c-8a3a-4e58-8063-b86c2f3e84d8";

    @Mock
    private PamQualificationProperties pamQualificationProperties;

    @Test
    void transformTest() {

        Mockito.when(pamQualificationProperties.getOperation()).thenReturn("getOperation");
        Mockito.when(pamQualificationProperties.getIngEntity()).thenReturn("getIngEntity");
        Mockito.when(pamQualificationProperties.getUuidCode()).thenReturn("getUuidCode");
        Request request = new PamQualificationReqTransformer(URL_FORMAT, pamQualificationProperties).transform(ORGANIZATION_ID);
        assertThat(request.uri()).isEqualTo("/api/qualification-checks/parties");
        assertThat(request.method()).isEqualTo(Method.Post());
    }

}
