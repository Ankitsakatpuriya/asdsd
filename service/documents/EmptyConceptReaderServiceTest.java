package com.ing.bankguarantees.service.documents;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Slf4j
@ExtendWith(MockitoExtension.class)
class EmptyConceptReaderServiceTest {

    @Mock
    private EmptyConceptReaderService emptyConceptReaderService;

    @Mock
    @Qualifier("workStealingPool")
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(1);
        emptyConceptReaderService = new EmptyConceptReaderService();
    }

    private static Stream<Arguments> arguments() {
        List<String> languages = List.of("en", "fr", "nl");
        return Arrays.stream(BankGuaranteeCode.values())
                .filter(bankGuaranteeCode -> !bankGuaranteeCode.equals(BankGuaranteeCode.CUSTOMIZED_TEXT)
                        && !bankGuaranteeCode.equals(BankGuaranteeCode.DCK_CDC))
                .map(bankGuaranteeCode -> languages
                        .stream()
                        .map(language -> Arguments.of(bankGuaranteeCode, language)))
                .flatMap(argumentsStream -> argumentsStream);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void convert(BankGuaranteeCode bankGuaranteeCode, String language) {
        var sampleDocumentResponse = emptyConceptReaderService.getEmptyConcept(bankGuaranteeCode, language).join();

        assertThat(sampleDocumentResponse.getFileName()).isNotNull().isEqualTo(bankGuaranteeCode.name() + "-" + language + ".pdf");
        assertThat(sampleDocumentResponse.getFileContent().getByteArray()).isNotNull()
                .isEqualTo(MockHelper.readFile(bankGuaranteeCode, language));
    }


    @Test
    void checkEmptyDocumentForInvalidLanguage() {
        BgosException exception = assertThrows(BgosException.class, () -> emptyConceptReaderService.getEmptyConcept(REAL_ESTATE, "hi"));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }
}
