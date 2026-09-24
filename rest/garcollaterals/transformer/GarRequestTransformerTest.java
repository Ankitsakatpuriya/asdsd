package com.ing.bankguarantees.remote.rest.garcollaterals.transformer;

import com.ing.bankguarantees.models.Identifier;
import com.ing.bankguarantees.remote.rest.garcollaterals.model.request.GarCollateralsInput;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GarRequestTransformerTest {

    public static final String IDENTIFIER_CLIENT = "CLIENT-999";
    private GarRequestTransformer transformer;

    private static final String MOCK_URL = "https://mock-api/gar-collaterals";
    private static final String APP_NAME = "TestService";

    @BeforeEach
    void setUp() {
        transformer = new GarRequestTransformer(MOCK_URL, APP_NAME);
    }

    @Test
    void shouldTransformInputToHttpRequest() {
        // Given
        GarCollateralsInput input = new GarCollateralsInput();
        input.setLocale(Locale.ENGLISH);
        Identifier identifier = new Identifier().builder().type(IDENTIFIER_CLIENT).build();
        input.setIdentifier(identifier);

        // When
        Request result = transformer.transform(input);

        // Then
        assertNotNull(result);
        assertEquals(Method.Post(), result.method());
        assertTrue(result.uri().contains("gar-collaterals"));
        assertTrue(result.headerMap().get("Content-Type").get().contains("application/json"));
        assertEquals("BE-BG", result.headerMap().get("X-ING-RCID").get());
        assertEquals(APP_NAME, result.headerMap().get("X-ING-SOURCE").get());
    }

}
