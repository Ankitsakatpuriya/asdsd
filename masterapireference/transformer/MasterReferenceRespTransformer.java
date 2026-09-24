package com.ing.bankguarantees.remote.rest.masterapireference.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.masterapireference.model.MasterReferenceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MasterReferenceRespTransformer implements Transformer<MasterReferenceResponse, String> {

    @Override
    public String transform(MasterReferenceResponse masterReferenceResponse) {
        log.info("MasterReferenceRespTransformer [transform] Receive response from master reference endpoint ");
        return masterReferenceResponse.masterReferenceId();
    }
}
