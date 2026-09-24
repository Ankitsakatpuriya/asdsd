package com.ing.bankguarantees.service.financial;

import com.ing.bankguarantees.models.response.CreditOverviewResponse;
import com.ing.bankguarantees.models.response.CreditOverviewResponse.IdentifierResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementOutput;
import com.ing.bankguarantees.remote.rest.kfccreditoverview.model.response.CustomerArrangementResponse;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CommonUtils;
import com.ing.bankguarantees.utils.ConstantUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreditOverviewDetailServiceTest {

    private static final String IBAN_NUMBER = "BE78335024399586";
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    @Mock
    private FinancialDetailService financialDetailService;

    @Mock
    private ClientGateway<String, Optional<CustomerArrangementOutput>, CustomerArrangementResponse> customerArrangementClientGateway;

    @InjectMocks
    private CreditOverviewDetailService creditOverviewDetailService;

    @Test
    void getCreditOverviewDetailPositive() {
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(BigDecimal.valueOf(CommonUtils.ibanToBban(IBAN_NUMBER)), AMOUNT);
        CustomerArrangementOutput customerArrangementOutput = MockHelper.getCustomerArrangementOutput();
        given(customerArrangementClientGateway.performRequest(anyString())).willReturn(CompletableFuture.completedFuture(Optional.of(customerArrangementOutput)));
        given(financialDetailService.getCreditLines(any())).willReturn(CompletableFuture.completedFuture(Optional.of(creditBalanceOutput)));
        CreditOverviewResponse creditOverviewResponse = creditOverviewDetailService.getCreditOverviewDetail(IBAN_NUMBER).join();
        assertThat(creditOverviewResponse).isNotNull();
        assertThat(creditOverviewResponse.availableAmount()).isEqualTo(AMOUNT);
        assertThat(creditOverviewResponse.codeLanguage()).isEqualTo(customerArrangementOutput.codeLanguage());
        assertThat(creditOverviewResponse.codeXy()).isEqualTo(customerArrangementOutput.codeXy());
        assertThat(creditOverviewResponse.contraNotariety()).isEqualTo(customerArrangementOutput.contraNotariety());
        assertThat(creditOverviewResponse.ssomi()).isEqualTo(customerArrangementOutput.ssomi());
        assertThat(creditOverviewResponse.loanStatusManual()).isEqualTo(customerArrangementOutput.loanStatusManual());
        assertThat(creditOverviewResponse.respCommercialManagerNo()).isEqualTo(customerArrangementOutput.respCommercialManagerNo());
        assertThat(creditOverviewResponse.segMis()).isEqualTo(customerArrangementOutput.segMis());
        assertThat(creditOverviewResponse.status()).isEqualTo(creditBalanceOutput.get(0).getRangeStatus());
        assertThat(creditOverviewResponse.creditType()).isEqualTo(creditBalanceOutput.get(0).getProductCode());
        assertThat(getIdentifierValue(creditOverviewResponse.identifiers(), ConstantUtils.CSI)).isEqualTo(customerArrangementOutput.csiIdentifier());
        assertThat(getIdentifierValue(creditOverviewResponse.identifiers(), ConstantUtils.ITV_TYPE)).isEqualTo(customerArrangementOutput.itvIdentifier());
        assertThat(getIdentifierValue(creditOverviewResponse.identifiers(), ConstantUtils.GRID_TYPE)).isEqualTo(customerArrangementOutput.gridId());
        assertThat(creditOverviewResponse.creditType()).isEqualTo(creditBalanceOutput.get(0).getProductCode());
        assertThat(creditOverviewResponse.creditType()).isEqualTo(creditBalanceOutput.get(0).getProductCode());


    }

    @Test
    void getCreditOverviewDetailForDefaultValues() {

        given(customerArrangementClientGateway.performRequest(anyString())).willReturn(CompletableFuture.completedFuture(Optional.of(CustomerArrangementOutput.builder().build())));
        given(financialDetailService.getCreditLines(any())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        CreditOverviewResponse creditOverviewResponse = creditOverviewDetailService.getCreditOverviewDetail(IBAN_NUMBER).join();
        assertThat(creditOverviewResponse).isNotNull();
        assertThat(creditOverviewResponse.availableAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(creditOverviewResponse.codeLanguage()).isEqualTo(0);
        assertThat(creditOverviewResponse.codeXy()).isEqualTo(0);
        assertThat(creditOverviewResponse.contraNotariety()).isEqualTo(0);
        assertThat(creditOverviewResponse.ssomi()).isNull();
        assertThat(creditOverviewResponse.loanStatusManual()).isEqualTo(0);
        assertThat(creditOverviewResponse.respCommercialManagerNo()).isEqualTo(0);
        assertThat(creditOverviewResponse.segMis()).isEqualTo(0);
        assertThat(creditOverviewResponse.status()).isEqualTo(0);
        assertThat(creditOverviewResponse.creditType()).isEqualTo(0);
        assertThat(creditOverviewResponse.identifiers()).isEmpty();
    }


    @Test
    void getCreditOverviewDetailForEmptyCustomerArrangement() {


        given(customerArrangementClientGateway.performRequest(anyString())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        given(financialDetailService.getCreditLines(any())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        CreditOverviewResponse creditOverviewResponse = creditOverviewDetailService.getCreditOverviewDetail(IBAN_NUMBER).join();
        assertThat(creditOverviewResponse).isNotNull();
        assertThat(creditOverviewResponse.availableAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(creditOverviewResponse.codeLanguage()).isEqualTo(0);
        assertThat(creditOverviewResponse.codeXy()).isEqualTo(0);
        assertThat(creditOverviewResponse.contraNotariety()).isEqualTo(0);
        assertThat(creditOverviewResponse.ssomi()).isNull();
        assertThat(creditOverviewResponse.loanStatusManual()).isEqualTo(0);
        assertThat(creditOverviewResponse.respCommercialManagerNo()).isEqualTo(0);
        assertThat(creditOverviewResponse.segMis()).isEqualTo(0);
        assertThat(creditOverviewResponse.status()).isEqualTo(0);
        assertThat(creditOverviewResponse.creditType()).isEqualTo(0);
        assertThat(creditOverviewResponse.identifiers()).isEmpty();
    }

    private String getIdentifierValue(List<IdentifierResponse> identifiers, String type) {

        return identifiers.stream()
                .filter(identifierResponse -> identifierResponse.type().equals(type))
                .findFirst()
                .map(IdentifierResponse::value)
                .orElse(null);

    }
}
