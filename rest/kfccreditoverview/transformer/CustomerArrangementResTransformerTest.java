package com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer;

import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CustomerArrangementResTransformerTest {


    @Test
    void transformTest() {

        CustomerArrangementResponse customerArrangementResponse = MockHelper.getCustomerArrangementResponse();
        Optional<CustomerArrangementOutput> result = new CustomerArrangementsRespTransformer().transform(customerArrangementResponse);
        assertThat(result).isPresent();
        assertThat(result.get()).isNotNull();
        assertThat(result.get().codeLanguage()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).codeLanguage());
        assertThat(result.get().codeXy()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).codeXy());
        assertThat(result.get().contraNotariety()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).contraNotariety());
        assertThat(result.get().ssomi()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).ssomi());
        assertThat(result.get().segMis()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).segMis());
        assertThat(result.get().gridId()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).gridId());
        assertThat(result.get().loanStatusManual()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).loanStatusManual());
        assertThat(result.get().respCommercialManagerNo()).isEqualTo(customerArrangementResponse.signaleticInformation().get(0).respCommercialManagerNo());
    }

    @Test
    void transformTestEmptyResponse() {

        CustomerArrangementResponse customerArrangementResponse = CustomerArrangementResponse.builder().build();
        Optional<CustomerArrangementOutput> result = new CustomerArrangementsRespTransformer().transform(customerArrangementResponse);
        assertThat(result).isEmpty();
    }
}
