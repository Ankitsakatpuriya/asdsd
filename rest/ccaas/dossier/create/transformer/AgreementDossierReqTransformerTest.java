package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.AgreementDossierDataInput;
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
class AgreementDossierReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/agreement-dossiers";

    @Test
    void transformTest() {
        AgreementDossierDataInput agreementDossierDataInput =
                RequestAdapter.getRequestAgreementIn("reqResId", "requestId","legalEntityId");
        Request request = new AgreementDossierReqTransformer(URL_FORMAT)
                .transform(agreementDossierDataInput);
        assertThat(request.uri()).isEqualTo("/gcc/case-management/agreement-dossiers");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isNotNull();
    }
}
