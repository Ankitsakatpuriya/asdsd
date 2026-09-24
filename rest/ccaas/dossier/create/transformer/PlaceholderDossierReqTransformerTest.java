package com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer;

import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DocumentPlaceHolderIn;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PlaceholderDossierReqTransformerTest {
    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/agreement-dossiers/{dossier_id}/documents";

    @Test
    void transformTest() {
        DocumentPlaceHolderIn documentPlaceHolderIn =
                RequestAdapter.getRequestPlaceholderIn("reqResId", DocumentType.CONTRACT, Locale.UK);
        Request request = new PlaceholderDossierReqTransformer(URL_FORMAT)
                .transform(documentPlaceHolderIn);
        assertThat(request.uri()).isEqualTo("/gcc/case-management/agreement-dossiers/reqResId/documents");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isNotNull();
    }

    @Test
    void transformTestForBGDraft() {
        DocumentPlaceHolderIn documentPlaceHolderIn =
                RequestAdapter.getRequestPlaceholderIn("reqResId", DocumentType.BG_DRAFT, Locale.UK);
        Request request = new PlaceholderDossierReqTransformer(URL_FORMAT)
                .transform(documentPlaceHolderIn);
        assertThat(request.uri()).isEqualTo("/gcc/case-management/agreement-dossiers/reqResId/documents");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.contentString()).isNotNull();
    }
}
