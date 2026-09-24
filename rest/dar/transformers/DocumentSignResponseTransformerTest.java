package com.ing.bankguarantees.remote.rest.dar.transformers;

import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse.DarResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSignResponseTransformerTest {

    private static final String DAR_UUID = "b59f13b6-0fee-468a-8c7f-c1df1a2b5453";


    @Test
    void transformPositive() {
        DarListResponse darListResponse = MockHelper.getDarListResponse(DAR_UUID);
        Optional<DarResponse> optional = new DocumentSignResponseTransformer().transform(darListResponse);
        assertThat(optional).isNotEmpty();
        assertThat(optional.get().getDarUuid()).isEqualTo(DAR_UUID);
    }

    @Test
    void transformEmptyResponse() {
        DarListResponse darListResponse = MockHelper.getDarListResponse(DAR_UUID);
        darListResponse.setData(Collections.emptyList());
        Optional<DarResponse> optional = new DocumentSignResponseTransformer().transform(darListResponse);
        assertThat(optional).isEmpty();
    }


}
