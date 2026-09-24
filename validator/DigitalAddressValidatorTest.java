package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.models.enums.DigitalAddressType;
import com.ing.bankguarantees.models.request.InstructingPartyPayload.DigitalAddressPayload;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.validation.validator.DigitalAddressValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalAddressValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"email@email.com", "email.email@company.commmmmmmmmmmmmmmmmmm"})
    void isValidEmail(String value) {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress(value, DigitalAddressType.EMAIl.getType());
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isTrue();
    }

    @Test
    void isValidNullAddress() {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        boolean valid = underTest.isValid(null, null);
        assertThat(valid).isTrue();
    }


    @ParameterizedTest
    @ValueSource(strings = {"email", "email.companycom", ""})
    void isInvalidEmailValid(String value) {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress(value, DigitalAddressType.EMAIl.getType());
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"012456789", "0124567891"})
    void isValidPhone(String value) {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress(value, DigitalAddressType.PHONE_NUMBER.getType());
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"012", " ", "", "test", "0112312423423423423423", "+123123123"})
    void isInvalidPhoneValid(String value) {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress(value, DigitalAddressType.PHONE_NUMBER.getType());
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isFalse();
    }

    @Test
    void isInvalidType() {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress("email@email.com", "test");
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isFalse();
    }

    @Test
    void isInvalidNullType() {
        DigitalAddressValidator underTest = new DigitalAddressValidator();
        DigitalAddressPayload digitalAddress = MockHelper.getDigitalAddress("email@email.com", null);
        boolean valid = underTest.isValid(digitalAddress, null);
        assertThat(valid).isFalse();
    }
}