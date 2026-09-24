package com.ing.bankguarantees.remote.rest.pega.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PegaCreateCaseRespTransformer implements Transformer<PegaCreateCaseResponse, String> {
    @Override
    public String transform(PegaCreateCaseResponse pegaCreateCaseResponse) {
        log.info("PegaCreateCaseRespTransformer [transform] receive response from Pega ");
        return pegaCreateCaseResponse.id();
    }
}
