package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.validation.validator.CbeNumberValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class CbeNumberValidatorTest {

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @ParameterizedTest
    @ValueSource(strings = {"123456789",""})
    void isValidCBE(String value) {
        CbeNumberValidator validator = new CbeNumberValidator();
        boolean valid = validator.isValid(value, constraintValidatorContext);
        assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcgd","1234.123.456"})
    void isInvalidCBE(String value) {
        CbeNumberValidator validator = new CbeNumberValidator();
      boolean valid = validator.isValid(value, constraintValidatorContext);
        assertThat(valid).isFalse();
    }

}
