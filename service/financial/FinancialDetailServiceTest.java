package com.ing.bankguarantees.service.financial;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.response.FinancialDetailResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.accountbalance.model.request.AccountBalanceInput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceOutput;
import com.ing.bankguarantees.remote.rest.accountbalance.model.response.AccountBalanceResponse;
import com.ing.bankguarantees.remote.rest.amsklc.accountloan.response.AccountLoanResponse;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.request.CreditBalanceInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditBalanceOutput;
import com.ing.bankguarantees.remote.rest.cla.creditlinebalance.model.response.CreditLineBalanceResponse;
import com.ing.bankguarantees.remote.rest.productagreement.ProductAgreementProperties;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementAccount;
import com.ing.bankguarantees.remote.rest.productagreement.model.response.ProductAgreementResponse;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FinancialDetailServiceTest {

    @InjectMocks
    private FinancialDetailService financialDetailService;

    @Mock
    private ClientGateway<String, List<ProductAgreementAccount>, ProductAgreementResponse> productAgreementAccountClientGateway;

    @Mock
    private ClientGateway<AccountBalanceInput, List<AccountBalanceOutput>, AccountBalanceResponse> accountBalanceGateway;

    @Mock
    private ClientGateway<CreditBalanceInput, List<CreditBalanceOutput>, CreditLineBalanceResponse> creditLineClientGateway;

    @Mock
    private ClientGateway<String, Optional<BigDecimal>, AccountLoanResponse> acctLoanGateway;

    @Mock
    private ProductAgreementProperties productAgreementProperties;

    @Mock
    @Qualifier("workStealingPool")
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(1);
        financialDetailService = new FinancialDetailService(productAgreementProperties, accountBalanceGateway, creditLineClientGateway,
                productAgreementAccountClientGateway, acctLoanGateway);
    }

    @Test
    void getFinancialDetails() {
        CreditLineBalanceResponse creditLineResponse = MockHelper.getCreditLineResponse();
        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        CreditBalanceInput creditLineInput = RequestAdapter.getCreditLineRequest(productAgreement);
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(creditLineResponse, creditLineInput);
        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        given(productAgreementProperties.getCurrentAccountProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementProperties.getCreditLineProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getProductAgreementAccount()));
        given(accountBalanceGateway.performRequest(any(AccountBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(acctBalanceOutput));
        given(acctLoanGateway.performRequest(anyString())).willReturn(CompletableFuture.completedFuture(Optional.of(BigDecimal.valueOf(12345L))));
        given(creditLineClientGateway.performRequest(any(CreditBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(creditBalanceOutput));

        FinancialDetailResponse financialDetails = financialDetailService.getFinancialDetails("2456234").join();

        assertNotNull(financialDetails);
        assertThat(financialDetails.getCreditLineDetails()).isNotNull();
        assertThat(financialDetails.getCurrentAccountDetails()).isNotNull();
        assertThat(financialDetails.getCurrentAccountDetails().getAccounts()).isNotNull();
        assertThat(financialDetails.getCreditLineDetails().getContractDetails()).isNotNull();
        assertThat(financialDetails.getCreditLineDetails().getContractDetails().get(0).getCreditLineAccountNumber())
                .isEqualTo(BigDecimal.valueOf(384031884085L));
        assertThat(financialDetails.getCreditLineDetails().getContractDetails().get(0).getBalanceAmount())
                .isEqualTo(BigDecimal.valueOf(79998));
        assertThat(financialDetails.getCreditLineDetails().getContractDetails().get(0).getIbanNumber())
                .isEqualTo("BE89384031884085");

    }

    @Test
    void getFinancialDetailsAccountLoanRetuenNull() {
        CreditLineBalanceResponse creditLineResponse = MockHelper.getCreditLineResponse();
        ProductAgreementAccount productAgreement = MockHelper.getCreditLineProductAgreement();
        CreditBalanceInput creditLineInput = RequestAdapter.getCreditLineRequest(productAgreement);
        List<CreditBalanceOutput> creditBalanceOutput = MockHelper.getCreditBalanceOutput(creditLineResponse, creditLineInput);
        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        given(productAgreementProperties.getCurrentAccountProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementProperties.getCreditLineProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getProductAgreementAccount()));
        given(accountBalanceGateway.performRequest(any(AccountBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(acctBalanceOutput));
        given(acctLoanGateway.performRequest(anyString())).willReturn(CompletableFuture.completedFuture(Optional.empty()));
        given(creditLineClientGateway.performRequest(any(CreditBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(creditBalanceOutput));

        var financialDetails = financialDetailService.getFinancialDetails("2456234");

        CompletionException exception = assertThrows(CompletionException.class, financialDetails::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("BGOS-00-001");

    }


    @Test
    void getFinancialDetailsProductAgreementRespEmpty() {
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(null));

        var financialDetails = financialDetailService.getFinancialDetails("2456234");
        CompletionException exception = assertThrows(CompletionException.class, financialDetails::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-001");
    }

    @Test
    void getFinancialDetailsCreditLineResponseEmpty() {
        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        given(productAgreementProperties.getCurrentAccountProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementProperties.getCreditLineProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getProductAgreementAccount()));
        given(accountBalanceGateway.performRequest(any(AccountBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(acctBalanceOutput));
        when(creditLineClientGateway.performRequest(any(CreditBalanceInput.class))).thenAnswer(answer -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            BgosException bgosExcep = new BgosException(ErrorCode.INVALID_RESPONSE_FROM_CLIENT);
            future.completeExceptionally(bgosExcep);
            return future;
        });

        var financialDetails = financialDetailService.getFinancialDetails("2456234");
        CompletionException exception = assertThrows(CompletionException.class, financialDetails::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-001");
    }

    @Test
    void getFinancialDetailsCreditLineResponseEmpty204() {
        AccountBalanceResponse acctBalanceResponse = MockHelper.getAcctBalanceResponse();
        AccountBalanceInput accountBalanceInput = RequestAdapter.getAccountDetailRequest(MockHelper.getAcctBalanceProductAgreement());
        List<AccountBalanceOutput> acctBalanceOutput = MockHelper.getAcctBalanceOutput(acctBalanceResponse, accountBalanceInput);
        given(productAgreementProperties.getCurrentAccountProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementProperties.getCreditLineProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(MockHelper.getProductAgreementAccount()));
        given(accountBalanceGateway.performRequest(any(AccountBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(acctBalanceOutput));
        when(creditLineClientGateway.performRequest(any(CreditBalanceInput.class))).thenAnswer(answer -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new Exception("BGOS-04-003"));
            return future;
        });

        var financialDetails = financialDetailService.getFinancialDetails("2456234").join();
        assertNotNull(financialDetails);
        assertThat(financialDetails.getCreditLineDetails()).isNotNull();
        assertThat(financialDetails.getCurrentAccountDetails()).isNotNull();
        assertThat(financialDetails.getCurrentAccountDetails().getAccounts()).isNotNull();
        assertThat(financialDetails.getCreditLineDetails().getContractDetails()).isNotNull();
    }

    @Test
    void getFinancialDetailsCurrentAccountEmpty() {
        var productAgreementAccount = MockHelper.getProductAgreementAccount();
        productAgreementAccount.get(0).setProductType("");
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(productAgreementAccount));

        var financialDetails = financialDetailService.getFinancialDetails("2456234");
        CompletionException exception = assertThrows(CompletionException.class, financialDetails::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-020");
    }

    @Test
    void getFinancialDetailsCurrentAccountResponseEmpty() {
        var productAgreementAccount = MockHelper.getProductAgreementAccount();
        given(productAgreementAccountClientGateway.performRequest(anyString()))
                .willReturn(CompletableFuture.completedFuture(productAgreementAccount));
        given(productAgreementProperties.getCurrentAccountProductTypes()).willReturn(Set.of("BE_ING_PROF_CRN_AC"));
        given(accountBalanceGateway.performRequest(any(AccountBalanceInput.class)))
                .willReturn(CompletableFuture.completedFuture(null));
        var financialDetails = financialDetailService.getFinancialDetails("2456234");
        CompletionException exception = assertThrows(CompletionException.class, financialDetails::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-001");
    }
}
