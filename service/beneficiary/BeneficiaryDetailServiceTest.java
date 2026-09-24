package com.ing.bankguarantees.service.beneficiary;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.response.BeneficiaryWarningResponse;
import com.ing.bankguarantees.models.response.DefaultBeneficiaryResponse;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryDetailServiceTest {


    @InjectMocks
    private BeneficiaryDetailService beneficiaryDetailService;
    @Mock
    private BeneficiaryProperties beneficiaryProperties;


    @Test
    void fetchDefaultBeneficiaryPositive() {
        lenient().when(beneficiaryProperties.getOvam()).thenReturn(MockHelper.getBeneficiaryData());

        DefaultBeneficiaryResponse defaultBeneficiaryResponse = beneficiaryDetailService.getDefaultBeneficiaryDetails(
                BankGuaranteeCode.OVAM, Locale.ENGLISH).join();
        assertThat(defaultBeneficiaryResponse).isNotNull();
        assertThat(defaultBeneficiaryResponse.cityName()).isNotBlank();
        assertThat(defaultBeneficiaryResponse.name()).isEqualTo("name");
        assertThat(defaultBeneficiaryResponse.cityName()).isEqualTo("cityname");
        assertThat(defaultBeneficiaryResponse.postalCode()).isEqualTo("postalcode");
        assertThat(defaultBeneficiaryResponse.countryCode()).isEqualTo("228");
        assertThat(defaultBeneficiaryResponse.firstAddress()).isEqualTo("first Address");
        assertThat(defaultBeneficiaryResponse.email()).isEqualTo("email");
        assertThat(defaultBeneficiaryResponse.pageDescription()).isEqualTo("Ovam");
    }

    @Test
    void fetchDefaultBeneficiaryNegative() {
        lenient().when(beneficiaryProperties.getOvam()).thenReturn(MockHelper.getBeneficiaryData());

        var defaultBeneficiaryResponse = beneficiaryDetailService.getDefaultBeneficiaryDetails(
                BankGuaranteeCode.PERFORMANCE_BOND, Locale.ENGLISH);
        CompletionException exception = assertThrows(CompletionException.class, defaultBeneficiaryResponse::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-024");

    }

    @Test
    void shouldReturnWarningText_WhenGuaranteeCodeIsNotCustomizedAndCbeExists() throws Exception {
        List<String> cbeNos = List.of("0203430576");
        Map<String, String> warningText = Map.of(
                "en", "English Warning",
                "fr", "French Warning"
        );

        when(beneficiaryProperties.getCbeNos()).thenReturn(cbeNos);
        when(beneficiaryProperties.getWarning()).thenReturn(warningText);

        BeneficiaryWarningResponse result = beneficiaryDetailService.getWarning(BankGuaranteeCode.OPERATORS_TRANSPORT, "0203430576", Locale.FRENCH).get();
        BeneficiaryWarningResponse expected = new BeneficiaryWarningResponse("0203430576", "French Warning");
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmpty_WhenGuaranteeCodeIsCustomizedText() throws Exception {
        BeneficiaryWarningResponse result = beneficiaryDetailService.getWarning(BankGuaranteeCode.CUSTOMIZED_TEXT, "0203430576", Locale.FRENCH).get();
        BeneficiaryWarningResponse expected = new BeneficiaryWarningResponse("0203430576", "");
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmpty_WhenCbeDoesNotExist() throws Exception {
        BeneficiaryWarningResponse result = beneficiaryDetailService.getWarning(BankGuaranteeCode.OPERATORS_TRANSPORT, "0203430576", Locale.FRENCH).get();
        BeneficiaryWarningResponse expected = new BeneficiaryWarningResponse("0203430576", "");
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmpty_WhenLocaleIsNotConfigured() throws Exception {
        BeneficiaryWarningResponse result = beneficiaryDetailService.getWarning(BankGuaranteeCode.OPERATORS_TRANSPORT, "0203430576", Locale.GERMAN).get();
        BeneficiaryWarningResponse expected = new BeneficiaryWarningResponse("0203430576", "");
        assertEquals(expected, result);
    }


}
