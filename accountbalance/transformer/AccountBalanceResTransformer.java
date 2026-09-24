package com.ing.bankguarantees.remote.rest.accountbalance.transformer;


import com.ing.bankguarantees.remote.common.InputTransformer;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;


@Slf4j
@Component("acctBalRespTransformer")
public class AccountBalanceResTransformer implements InputTransformer<AccountBalanceResponse, AccountBalanceInput, List<AccountBalanceOutput>> {

    @Override
    public List<AccountBalanceOutput> transform(AccountBalanceResponse accountBalanceResponse, AccountBalanceInput input) {
        log.info("AccountBalanceResTransformer [transform] receive response for Account balance Search API {} ", accountBalanceResponse);
        return accountBalanceResponse.getAccounts()
                .stream()
                .map(account -> prepareAccountBalanceOutput(account, input))
                .toList();
    }

    private AccountBalanceOutput prepareAccountBalanceOutput(AccountBalanceResponse.AccountResponse accountResponse,
                                                             AccountBalanceInput accountBalanceInput) {
        return AccountBalanceOutput.builder()
                .accountName(accountBalanceInput.getAccountName())
                .uuid(accountBalanceInput.getUuid())
                .ibanNumber(accountResponse.getIbanNumber())
                .accountCurrency(accountResponse.getAccountCurrency())
                .balanceAmount(accountResponse.getBalanceAmount())
                .accountStatus(accountResponse.getAccountStatus())
                .accountProductId(accountResponse.getAccountProductId())
                .build();
    }

}
