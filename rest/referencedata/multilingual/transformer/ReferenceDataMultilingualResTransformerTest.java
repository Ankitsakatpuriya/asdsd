package com.ing.bankguarantees.remote.rest.referencedata.multilingual.transformer;

import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDataMultilingualResTransformerTest {


    @Test
    void transformTest() {
        ReferenceDataMultilingualResponse referenceDataResponse = MockHelper.getReferenceDataMLResponse();
        List<ReferenceDataMultilingualResponse.ReferenceData> response = new ReferenceDataMultilingualResTransformer().transform(referenceDataResponse);
        assertThat(response).isNotEmpty().isEqualTo(referenceDataResponse.getData());
    }

    @Test
    void transformNullTest() {
        List<ReferenceDataMultilingualResponse.ReferenceData> response = new ReferenceDataMultilingualResTransformer().transform(new ReferenceDataMultilingualResponse());
        assertThat(response).isEmpty();
    }
}
