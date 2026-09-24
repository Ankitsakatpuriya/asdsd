package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.guaranteetype.WoodsDischarge;
import com.ing.bankguarantees.models.request.GuaranteeDetailsPayload;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.validation.validator.WoodsRequestValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WoodsRequestValidatorTest {

    private WoodsRequestValidator woodsRequestValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @BeforeEach
    void setUp() {
        woodsRequestValidator = new WoodsRequestValidator();
    }


    @Test
    void validateWoodsDischarge_positive() {

        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BG_DISCHARGE);
        WoodsDischarge woodsDischarge = (WoodsDischarge) payload.getBankGuarantee();
        woodsDischarge.setSalePrice(BigDecimal.valueOf(468));
        payload.setBgAmount(BigDecimal.valueOf(93.60));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isTrue();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
        verifyNoInteractions(constraintViolationBuilder);
    }

    @Test
    void validateWoodsDischarge_negative() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BG_DISCHARGE);
        WoodsDischarge woodsDischarge = (WoodsDischarge) payload.getBankGuarantee();
        woodsDischarge.setSalePrice(BigDecimal.valueOf(468));
        payload.setBgAmount(BigDecimal.valueOf(94.60));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isFalse();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate("BG Amount should not exceed the sale price with %20 up to most 6000");
        verify(constraintViolationBuilder).addConstraintViolation();
        verify(constraintValidatorContext, times(1)).disableDefaultConstraintViolation();
    }

    @Test
    void validateWoodsVLAPublic_positive() {
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BGVLA_PUBLIC);
        payload.setBgAmount(BigDecimal.valueOf(3));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isTrue();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
        verifyNoInteractions(constraintViolationBuilder);
    }

    @Test
    void validateWoodsVLAPublic_negative() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BGVLA_PUBLIC);
        payload.setBgAmount(BigDecimal.valueOf(4));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);

        assertThat(valid).isFalse();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate("BG Amount should be equal to sum of tranche amounts and cash amount in case provided");
        verify(constraintViolationBuilder).addConstraintViolation();
        verify(constraintValidatorContext, times(1)).disableDefaultConstraintViolation();
    }

    @Test
    void validateWoodsPrivate_positive() {
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BG_PRIVATE);
        payload.setBgAmount(BigDecimal.valueOf(3));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isTrue();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
        verifyNoInteractions(constraintViolationBuilder);
    }

    @Test
    void validateWoodsPrivate_negative() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BG_PRIVATE);
        payload.setBgAmount(BigDecimal.valueOf(4));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isFalse();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate("BG Amount should be equal to sum of tranche amounts and cash amount in case provided");
        verify(constraintViolationBuilder).addConstraintViolation();
        verify(constraintValidatorContext, times(1)).disableDefaultConstraintViolation();
    }

    @Test
    void validateWoodsWalPublic_positive() {
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BGWAL_PUBLIC);
        payload.setBgAmount(BigDecimal.valueOf(300));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isTrue();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
        verifyNoInteractions(constraintViolationBuilder);
    }


    @Test
    void validateWoodsWalPublic_negative() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
        GuaranteeDetailsPayload<BaseGuaranteeType> payload = MockHelper.getGuaranteeDetailsPayload(BankGuaranteeCode.WOODS_BGWAL_PUBLIC);
        payload.setBgAmount(BigDecimal.valueOf(600));
        boolean valid = woodsRequestValidator.validateAmount(payload, constraintValidatorContext);
        assertThat(valid).isFalse();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate("BG Amount should not exceed the sale price");
        verify(constraintViolationBuilder).addConstraintViolation();
        verify(constraintValidatorContext, times(1)).disableDefaultConstraintViolation();
    }


}
