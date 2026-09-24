package com.ing.bankguarantees.remote.rest.pega.transformer;

import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PegaCreateCaseRespTransformerTest {

    @Test
    void transformTest() {
        PegaCreateCaseResponse pegaResponse = MockHelper.getPegaResponse();
        String id = new PegaCreateCaseRespTransformer().transform(pegaResponse);
        assertThat(id).isEqualTo(pegaResponse.id());
    }
}
