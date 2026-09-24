package com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer;

import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRequestTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/gcc/case-management/documents/{documentId}/content";
    public static final String DOCUMENT_ID = "121212";

    @Test
    void transformTest() {
        Request request = new DocumentRequestTransformer(URL_FORMAT).transform(DOCUMENT_ID);
        assertThat(request.uri()).contains(String.format("/gcc/case-management/documents/%s/content", DOCUMENT_ID));
        assertThat(request.method()).isEqualTo(Method.Get());
        assertThat(request.contentString()).isNotNull();
    }
}
