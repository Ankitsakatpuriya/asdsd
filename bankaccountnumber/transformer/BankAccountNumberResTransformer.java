package com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component("bankAccountNumberRespTransformer")
public class BankAccountNumberResTransformer implements InputTransformer<BankAccountNumberResponse, String, String> {

    @Override
    public String transform(BankAccountNumberResponse bankAccountNumberResponse, String input) {
        log.info(C3LogMarker.marker, "BankAccountNumberResponse Transformer [transform] receive response for Bank AccountNumber API {} ", bankAccountNumberResponse);
        return bankAccountNumberResponse.getAccountNumber();
    }

}
