package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.request.BankGuaranteeRequestPayload;
import com.ing.bankguarantees.models.request.BeneficiaryPayload;
import com.ing.bankguarantees.models.enums.BeneficiaryType;
import com.ing.bankguarantees.validation.validator.DckBeneficiaryValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ing.bankguarantees.util.MockHelper.createBankGuaranteeRequestPayload;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DckBeneficiaryValidatorTest {

    private DckBeneficiaryValidator dckBeneficiaryValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setup() {

    }

    private static final String BG_PAYLOAD = "BGA/BGR/bg_request_payload.json";

    @Test
    void validateGuaranteePositive() {
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isTrue();
    }

    @Test
    void validateBeneficiaryPrivateIndividual() {
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        BeneficiaryPayload beneficiaryPayload = bankGuaranteeRequestPayload.getBeneficiary();
        beneficiaryPayload.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        beneficiaryPayload.setPrivateIndividual(null);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    void validateBeneficiaryCompany() {
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        BeneficiaryPayload beneficiaryPayload = bankGuaranteeRequestPayload.getBeneficiary();
        beneficiaryPayload.setBeneficiaryType(BeneficiaryType.COMPANY);
        beneficiaryPayload.setOrganisationName(null);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    void validateBeneficiaryPrivateIndividualBelgiumCitizen() {
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        BeneficiaryPayload beneficiaryPayload = bankGuaranteeRequestPayload.getBeneficiary();
        beneficiaryPayload.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        beneficiaryPayload.getPrivateIndividual().setBelgiumCitizen(true);
        beneficiaryPayload.getPrivateIndividual().setPrimaryIdentificationReference(null);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    void validateBeneficiaryPrivateIndividualNotBelgiumCitizen() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        BeneficiaryPayload beneficiaryPayload = bankGuaranteeRequestPayload.getBeneficiary();
        beneficiaryPayload.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        beneficiaryPayload.getPrivateIndividual().setBelgiumCitizen(false);
        beneficiaryPayload.getPrivateIndividual().setPrimaryBeneficiaryDob(null);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

    @Test
    void validateEmptyBeneficiary() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        dckBeneficiaryValidator = new DckBeneficiaryValidator();
        BankGuaranteeRequestPayload bankGuaranteeRequestPayload = createBankGuaranteeRequestPayload(BG_PAYLOAD);
        bankGuaranteeRequestPayload.setBeneficiary(null);
        boolean valid = dckBeneficiaryValidator.validateBeneficiary(bankGuaranteeRequestPayload, constraintValidatorContext);
        ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
        verify(constraintValidatorContext, times(1)).buildConstraintViolationWithTemplate(errorMessageCaptor.capture());
        assertThat(valid).isFalse();
        assertEquals("Beneficiary must not be empty.", errorMessageCaptor.getAllValues().get(0));
    }
}
