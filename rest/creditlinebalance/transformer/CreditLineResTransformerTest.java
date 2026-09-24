package com.ing.bankguarantees.remote.rest.creditlinebalance.transformer;

import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.transformer.CreditLineResTransformer;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CreditLineResTransformerTest {


    @Test
    void TransformTest() {

        ProductAgreementAccount creditLineProductAgreement = MockHelper.getCreditLineProductAgreement();
        CreditBalanceInput creditBalanceInput = RequestAdapter.getCreditLineRequest(creditLineProductAgreement);
        CreditLineBalanceResponse creditLineResponse = MockHelper.getCreditLineResponse();
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(creditLineResponse, creditBalanceInput);
        List<CreditBalanceOutput> listContractDetail = new CreditLineResTransformer().transform(creditLineResponse, creditBalanceInput);
        assertThat(listContractDetail).isNotEmpty().isEqualTo(creditBalanceOutput);

    }
}
