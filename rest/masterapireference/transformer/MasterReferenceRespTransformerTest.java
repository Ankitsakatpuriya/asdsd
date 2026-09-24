package com.ing.bankguarantees.remote.rest.masterapireference.transformer;

import com.ing.bankguarantees.remote.rest.masterapireference.model.MasterReferenceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MasterReferenceRespTransformerTest {

    private static final String EXPECTED_ID = "MR789";
    private static final String TIMESTAMP = "2025-08-08T07:30:00Z";
    @Test
    void testMasterReferenceId() {

        MasterReferenceResponse response = new MasterReferenceResponse(EXPECTED_ID, TIMESTAMP);
        MasterReferenceRespTransformer transformer = new MasterReferenceRespTransformer();
        String actualId = transformer.transform(response);

        assertEquals(EXPECTED_ID, actualId);
    }
}
