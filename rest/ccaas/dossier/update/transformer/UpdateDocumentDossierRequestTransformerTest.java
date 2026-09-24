package com.ing.bankguarantees.remote.rest.ccaas.dossier.update.transformer;

import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DossierType;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateDossierDataInput;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDocumentDossierRequestTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/agreement-dossiers";

    @Test
    void transformTest() {
        UpdateDossierDataInput updateDossierDataInput = RequestAdapter.getUpdateDossierDataInRequest("agrDossierId", DossierType.AGREEMENT_DOSSIER);
        Request request = new UpdateDocumentDossierRequestTransformer(URL_FORMAT).transform(updateDossierDataInput);
        assertThat(request.method()).isEqualTo(Method.Put());
        assertThat(request.contentString()).contains("\"code\":\"Accepted\"");
    }


}