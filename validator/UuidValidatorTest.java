package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.validation.validator.UuidValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UuidValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"e9b5fcdc-7f29-4c81-b804-8ee06603c0c7",""})
    void isValidUuid(String value) {
        UuidValidator underTest = new UuidValidator();
        boolean valid = underTest.isValid(value, null);
        assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"e9b5fcdc-7f29-4c81-{}b804-8ee06603c0c7"})
    void isInvalidUuid(String value) {
        UuidValidator underTest = new UuidValidator();
        boolean valid = underTest.isValid(value, null);
        assertThat(valid).isFalse();
    }

}