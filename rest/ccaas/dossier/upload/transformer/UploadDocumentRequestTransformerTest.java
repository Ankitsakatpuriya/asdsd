package com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.transformer;

import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.transform.UploadDocumentRequestTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.HeaderMap;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class UploadDocumentRequestTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/documents/{documentId}?version=major";
    private static final String DOCUMENT_ID = "344135tplow";

    @Test
    void transformTest() {

        UploadDocumentInput uploadDocumentInput = MockHelper.getUploadDocumentInput(DOCUMENT_ID, DocumentType.BG_FINAL);
        Request request = new UploadDocumentRequestTransformer(URL_FORMAT).transform(uploadDocumentInput);
        assertThat(request.uri()).isEqualTo(String.format("/gcc/case-management/documents/%s?version=major", DOCUMENT_ID));
        assertThat(request.method()).isEqualTo(Method.Post());
        HeaderMap headerMap = request.headerMap();
        assertThat(headerMap.getOrNull("Content-Type").contains("multipart/form-data"));
        String payload = request.contentString();
        assertThat(payload).isNotNull();
    }


}
