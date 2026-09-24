package com.ing.bankguarantees.remote.rest.pamqualification.transformer;

import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PamQualificationResTransformerTest {


    @Test
    void transformTest() {
        PamQualificationResponse pamQualificationResponse = MockHelper.getPamQualificationResponse();
        Boolean result = new PamQualificationResTransformer().transform(pamQualificationResponse);
        assertThat(result).isNotNull().isTrue();
    }

    @Test
    void transformNullTest() {
        PamQualificationResponse pamQualificationResponse = MockHelper.getPamQualificationResponse();
        pamQualificationResponse.getBeneficiary().setStatus("No");
        Boolean result = new PamQualificationResTransformer().transform(pamQualificationResponse);
        assertThat(result).isNotNull().isFalse();
    }
}
