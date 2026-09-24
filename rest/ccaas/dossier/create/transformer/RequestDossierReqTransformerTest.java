package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.RequestDossierDataInput;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RequestDossierReqTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/request-dossiers";

    @Test
    void transformTest() {
        RequestDossierDataInput requestDossierDataInput =
                RequestAdapter.getRequestDossierIn("reqResId", "legalEntityId");
        Request request = new RequestDossierReqTransformer(URL_FORMAT)
                .transform(requestDossierDataInput);
        assertThat(request.uri()).isEqualTo("/gcc/case-management/request-dossiers");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isNotNull();
    }
}
