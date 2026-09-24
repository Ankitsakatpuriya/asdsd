package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.request.BankGuaranteeRequestPayload;
import com.ing.bankguarantees.validation.validator.*;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ing.bankguarantees.util.MockHelper.createBankGuaranteeRequestPayload;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeRequestPayloadValidatorTest {

    @Mock
    private BeneficiaryValidator beneficiaryValidator;

    @Mock
    private GuaranteeValidator guaranteeValidator;

    @Mock
    private WoodsRequestValidator woodsRequestValidator;

    @Mock
    private CustomDocumentDetailsValidator customDocumentDetailsValidator;

    @InjectMocks
    private BankGuaranteeRequestPayloadValidator bankGuaranteeRequestPayloadValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;


    private static final String BG_PAYLOAD = "BGA/BGR/bg_request_payload.json";


    @Test
    void validateGuaranteePositive() {
        when(beneficiaryValidator.validateBeneficiary(any(), any())).thenReturn(true);
        when(guaranteeValidator.isValidGuarantee(any(), any())).thenReturn(true);
        when(woodsRequestValidator.validateAmount(any(), any())).thenReturn(true);
        when(customDocumentDetailsValidator.validateCustomDetails(any(), any())).thenReturn(true);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = bankGuaranteeRequestPayloadValidator.isValid(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isTrue();
    }

    @Test
    void validateGuaranteePositiveBeneficiaryInvalid() {
        when(beneficiaryValidator.validateBeneficiary(any(), any())).thenReturn(false);
        when(guaranteeValidator.isValidGuarantee(any(), any())).thenReturn(true);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = bankGuaranteeRequestPayloadValidator.isValid(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    void validateGuaranteePositiveGuaranteeInvalid() {
        when(beneficiaryValidator.validateBeneficiary(any(), any())).thenReturn(true);
        when(guaranteeValidator.isValidGuarantee(any(), any())).thenReturn(false);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = bankGuaranteeRequestPayloadValidator.isValid(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }
}
