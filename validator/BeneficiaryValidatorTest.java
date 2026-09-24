package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.request.BankGuaranteeRequestPayload;
import com.ing.bankguarantees.validation.validator.BeneficiaryValidator;
import com.ing.bankguarantees.validation.validator.DckBeneficiaryValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ing.bankguarantees.util.MockHelper.createBankGuaranteeRequestPayload;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryValidatorTest {

    @Mock
    private DckBeneficiaryValidator dckBeneficiaryValidator;

    private BeneficiaryValidator beneficiaryValidator;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;



    private static final String BG_PAYLOAD = "BGA/BGR/bg_request_payload.json";

    @BeforeEach
    void setup() {
        beneficiaryValidator = new BeneficiaryValidator(dckBeneficiaryValidator);
    }

    @Test
    void validateGuaranteePositive() {
        when(dckBeneficiaryValidator.validateBeneficiary(any(), any())).thenReturn(true);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = beneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isTrue();
    }

    @Test
    void validateGuaranteeNegative() {
        when(dckBeneficiaryValidator.validateBeneficiary(any(), any())).thenReturn(false);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = beneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }
}
