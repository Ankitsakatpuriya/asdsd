package com.ing.bankguarantees.remote.rest.emeafx.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.emeafx.model.response.FxRateConversionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class FxRateConversionResponseTransformer implements Transformer<FxRateConversionResponse, BigDecimal> {

    @Override
    public BigDecimal transform(FxRateConversionResponse response) {
        return response.convertedAmount();
    }


}
