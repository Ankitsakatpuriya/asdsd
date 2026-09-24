package com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.transformer;

import com.ing.bankguarantees.remote.rest.involveparty.externalidentifier.model.response.ExternalIdentifierListResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExtIdentRespTransformerTest {
    private static final String KBO_IDENTIFIER = "KBO_BE";
    private static final String UUID_IDENTIFIER = "UUID";
    private static final String IDENTIFIER_VALUE = "123456789";


    @Test
    void transformTest() {
        ExternalIdentifierListResponse externalIdentifierResponse = MockHelper.getExternalIdentifierResponse(KBO_IDENTIFIER, IDENTIFIER_VALUE);
        Optional<String> extIdentRespStr = new ExtIdentRespTransformer().transform(externalIdentifierResponse);
        assertThat(extIdentRespStr.get()).isNotEmpty().isEqualTo(IDENTIFIER_VALUE);
    }

    @Test
    void transformNullTest() {
        ExternalIdentifierListResponse externalIdentifierResponse = MockHelper.getExternalIdentifierResponse(UUID_IDENTIFIER, IDENTIFIER_VALUE);
        Optional<String> extIdentRespStr = new ExtIdentRespTransformer().transform(externalIdentifierResponse);
        assertThat(extIdentRespStr.isEmpty()).isTrue();
    }
}
