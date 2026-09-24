package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.response.CreditLineArrangementOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CreditLineArrangementResTransformer implements Transformer<CreditLineArrangementOutput, String> {

    @Override
    public String transform(CreditLineArrangementOutput creditLineArrangementOutput) {
        log.info("CreditLineArrangementResTransformer [transform ] Receive response for Credit Line Arrangement API ");

        return creditLineArrangementOutput.status();

    }
}
