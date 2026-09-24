package com.ing.bankguarantees.remote.rest.accountbalance.transformer.transformer;

import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
import com.ing.bankguarantees.remote.rest.accountbalance.transformer.AccountBalanceResTransformer;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountBalanceResTransformerTest {


    @Test
    void transformTest() {
        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        List<AccountBalanceOutput> listAccount = new AccountBalanceResTransformer().transform(acctBalanceResponse, accountBalanceInput);
        assertThat(listAccount).isNotEmpty().isEqualTo(acctBalanceOutput);
    }

}
