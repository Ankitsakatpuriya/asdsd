package com.ing.bankguarantees.remote.rest.ccaas.getdocument.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RemoteDocumentResponseTransformerTest {

    public static final String ORIGINAL_INPUT = "docId";

    @Test
    void transformTest() {
        ByteArrayResource transform = new DocumentResponseTransformer().transform(ORIGINAL_INPUT.getBytes());
        assertThat(transform).isEqualTo(new ByteArrayResource(ORIGINAL_INPUT.getBytes()));
    }
}
