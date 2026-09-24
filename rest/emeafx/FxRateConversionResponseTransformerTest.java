package com.ing.bankguarantees.remote.rest.emeafx;

import com.ing.bankguarantees.remote.rest.emeafx.model.response.FxRateConversionResponse;
import com.ing.bankguarantees.remote.rest.emeafx.transformer.FxRateConversionResponseTransformer;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FxRateConversionResponseTransformerTest {

    private static final BigDecimal AMOUNT = BigDecimal.valueOf(30.50);

    @Test
    void transformTestPositive() {
        FxRateConversionResponse fxRateConversionResponse = MockHelper.getFxRateConversionResponse(AMOUNT);
        BigDecimal convertedAmount = new FxRateConversionResponseTransformer().transform(fxRateConversionResponse);
        assertThat(convertedAmount).isEqualTo(AMOUNT);
    }
}
