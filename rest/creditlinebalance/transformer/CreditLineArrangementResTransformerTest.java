package com.ing.bankguarantees.remote.rest.creditlinebalance.transformer;

import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.response.CreditLineArrangementOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer.CreditLineArrangementResTransformer;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CreditLineArrangementResTransformerTest {


    @Test
    void TransformTest() {
        CreditLineArrangementOutput creditBalanceOutput = MockHelper.getCreditLineArrangementOutput();
        String status = new CreditLineArrangementResTransformer().transform(creditBalanceOutput);
        assertThat(status).isNotEmpty().isEqualTo("OK");

    }
}
