package com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.transformer;

import com.ing.bankguarantees.remote.rest.bankaccountnumber.model.response.BankAccountNumberResponse;
import com.ing.bankguarantees.remote.rest.bankaccountnumber.transformer.BankAccountNumberResTransformer;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BankAccountNumberResTransformerTest {

    @Test
    void transformTest() {
        BankAccountNumberResponse bankAccountNumberResponse = MockHelper.getBankAccountNumberResponse();
        var bankAccountNumberResTransformer = new BankAccountNumberResTransformer();
        var respose = bankAccountNumberResTransformer.transform(bankAccountNumberResponse, "requestId");
        assertThat(respose).isNotEmpty().isEqualTo("accountNumber");
    }
}
