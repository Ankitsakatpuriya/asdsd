package com.ing.bankguarantees.remote.rest.connectdot.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.connectdot.model.response.ConnectDotResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converts Response payload to required output
 */
@Component
@Slf4j
public class ConnectDotResTransformer implements Transformer<ConnectDotResponse, Boolean> {

    @Override
    public Boolean transform(ConnectDotResponse connectDotResponse) {
        log.info("ConnectDotResTransformer [transform] call");
        return Optional.ofNullable(connectDotResponse.getResults())
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .map(ConnectDotResponse.Result::isSuccess)
                .orElse(Boolean.FALSE);
    }

}
