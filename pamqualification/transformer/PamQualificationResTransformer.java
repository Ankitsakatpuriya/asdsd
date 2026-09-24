package com.ing.bankguarantees.remote.rest.pamqualification.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.pamqualification.model.response.PamQualificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;


@Slf4j
@Component
public class PamQualificationResTransformer implements Transformer<PamQualificationResponse, Boolean> {

    private static final String RESPONSE_YES = "YES";

    @Override
    public Boolean transform(PamQualificationResponse response) {
        log.info("PamQualificationResponseTransformer [transform] receive response Pam Qualification check.");
        return isNotEmpty(response.getBeneficiary()) && RESPONSE_YES.equalsIgnoreCase(response.getBeneficiary().getStatus());
    }


}
