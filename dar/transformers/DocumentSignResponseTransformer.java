package com.ing.bankguarantees.remote.rest.dar.transformers;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse;
import com.ing.bankguarantees.remote.rest.dar.model.response.DarListResponse.DarResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class DocumentSignResponseTransformer implements Transformer<DarListResponse, Optional<DarResponse>> {

    @Override
    public Optional<DarResponse> transform(DarListResponse darListResponse) {

        /*
           As per comment received from Dar Team, API will have only one item in list of Data.
           So, returning first element always
         */
        log.info("DocumentSignResponseTransformer [transform] receive response from doc sign");
        List<DarResponse> darResponses = darListResponse.getData();
        for (DarResponse darResponse : darResponses) {
            if (darResponse != null) return Optional.of(darResponse);
        }
        return Optional.empty();
    }
}

