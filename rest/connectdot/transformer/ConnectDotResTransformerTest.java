package com.ing.bankguarantees.remote.rest.connectdot.transformer;

import com.ing.bankguarantees.remote.rest.connectdot.model.response.ConnectDotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectDotResTransformerTest {

    private ConnectDotResTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new ConnectDotResTransformer();
    }

    @Test
    void shouldReturnTrueWhenFirstResultIsSuccessful() {
        ConnectDotResponse.Result result = new ConnectDotResponse.Result();
        result.setSuccess(true);

        ConnectDotResponse response = new ConnectDotResponse();
        response.setResults(List.of(result));

        Boolean transformed = transformer.transform(response);

        assertTrue(transformed, "Expected success to be true");
    }

    @Test
    void shouldReturnFalseWhenFirstResultIsUnsuccessful() {
        ConnectDotResponse.Result result = new ConnectDotResponse.Result();
        result.setSuccess(false);

        ConnectDotResponse response = new ConnectDotResponse();
        response.setResults(List.of(result));

        Boolean transformed = transformer.transform(response);

        assertFalse(transformed, "Expected success to be false");
    }

    @Test
    void shouldReturnFalseWhenResultsListIsEmpty() {
        ConnectDotResponse response = new ConnectDotResponse();
        response.setResults(Collections.emptyList());

        Boolean transformed = transformer.transform(response);

        assertFalse(transformed, "Expected false when results list is empty");
    }

    @Test
    void shouldReturnFalseWhenResultsIsNull() {
        ConnectDotResponse response = new ConnectDotResponse();
        response.setResults(null);

        Boolean transformed = transformer.transform(response);

        assertFalse(transformed, "Expected false when results is null");
    }
}
