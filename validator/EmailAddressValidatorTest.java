package com.ing.bankguarantees.validator;

import com.ing.bankguarantees.validation.validator.EmailAddressValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAddressValidatorTest {

    @ParameterizedTest
    @MethodSource("validEmails")
    void isValidEmail(String value) {
        EmailAddressValidator underTest = new EmailAddressValidator();
        boolean valid = underTest.isValid(value, null);
        assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidEmails")
    void isInvalidEmailValid(String value) {
        EmailAddressValidator underTest = new EmailAddressValidator();
        boolean valid = underTest.isValid(value, null);
        assertThat(valid).isFalse();
    }

    private static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of("plainaddress"),
                Arguments.of("#@%^%#\\(@\\#\\)@#.com"),
                Arguments.of("@example.com"),
                Arguments.of("Joe Smith <email@example.com>"),
                Arguments.of("email.domain.com"),
                Arguments.of("email@example@example.com"),
                Arguments.of(".email@example.com"),
                Arguments.of("email.@example.com"),
                Arguments.of("email..email@example.com"),
                Arguments.of("email@example.com (John Doe)"),
                Arguments.of("”(),:;<>[\\\\]@email.com"),
                Arguments.of("example\\\\ is”especially”not\\\\allowed@email.com"),
                Arguments.of("email@111.222.333.44444"),
                Arguments.of("email@example…com"),
                Arguments.of("email@example..com"),
                Arguments.of("email@example.-com"),
                Arguments.of("email@.example.com"),
                Arguments.of("email@example.com."),
                Arguments.of("")
        );
    }

    private static Stream<Arguments> validEmails() {
        return Stream.of(
                Arguments.of("user@example.com"),
                Arguments.of("user.name@example.co.uk"),
                Arguments.of("user+mailbox@example.com"),
                Arguments.of("user123@example.net"),
                Arguments.of("123user@example.com")
        );
    }
}