package com.ing.bankguarantees.remote.rest.aler.transformer;

import com.ing.bankguarantees.remote.rest.aler.model.response.AlerRetrieveSignatoriesResponse;
import com.ing.bankguarantees.remote.rest.aler.model.response.AlerSignatories;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AlerRetrieveSignatoriesResponseTransformerTest {

    private static final String ALER_SIGNATORY_RETRIEVE_RESPONSE = "BGA/SSA/retrive_signatory_response.json";

    @Test
    void transformTestPositive() {
        AlerRetrieveSignatoriesResponse response = MockHelper.createAlerRetrieveSignatoriesResponse(ALER_SIGNATORY_RETRIEVE_RESPONSE);
        AlerRetrieveSignatoriesResponse alerRetrieveSignatoriesResponse = MockHelper.getAlerRetrieveSignatoriesResponse(response.transactionId(), response.transactionStatus(), response.signatoryResponse(), null);
        AlerSignatories alerSignatoriesResult = new AlerRetrieveSignatoriesResponseTransformer().transform(alerRetrieveSignatoriesResponse);
        assertThat(alerSignatoriesResult.transactionId()).isEqualTo(response.transactionId());
        assertThat(alerSignatoriesResult.transactionStatus()).isEqualTo(response.transactionStatus());
    }

    @Test
    void transformTestError() {
        AlerRetrieveSignatoriesResponse response = MockHelper.createAlerRetrieveSignatoriesResponse(ALER_SIGNATORY_RETRIEVE_RESPONSE);
        AlerRetrieveSignatoriesResponse alerRetrieveSignatoriesResponse = MockHelper.getAlerRetrieveSignatoriesResponse(response.transactionId(), response.transactionStatus(), null, response.errorResponseDetails());
        AlerSignatories alerSignatoriesResult = new AlerRetrieveSignatoriesResponseTransformer().transform(alerRetrieveSignatoriesResponse);
        assertThat(alerSignatoriesResult.transactionId()).isEqualTo(response.transactionId());
        assertThat(alerSignatoriesResult.transactionStatus()).isEqualTo(response.transactionStatus());
    }
}