package com.ing.bankguarantees.database.transformer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class BankGuaranteeRequestTransformerTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";

    private BankGuaranteeRequestTransformer bankGuaranteeRequestTransformer;


    @BeforeEach
    void setUp() {
        bankGuaranteeRequestTransformer = new BankGuaranteeRequestTransformer();
    }

    @Test
    public void convertToDatabaseColumn() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        String expected = JsonUtils.getJsonFromObject(bankGuaranteeRequestData);
        String bgRequestJson = bankGuaranteeRequestTransformer.convertToDatabaseColumn(bankGuaranteeRequestData);

        assertThat(bgRequestJson).isNotNull().isEqualTo(expected);
    }

    @Test
    public void convertToEntityAttribute() {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        String jsonString = JsonUtils.getJsonFromObject(bankGuaranteeRequestData);
        BankGuaranteeRequestData actual = bankGuaranteeRequestTransformer.convertToEntityAttribute(jsonString);

        assertThat(actual).isNotNull().isEqualTo(bankGuaranteeRequestData);
    }

    @Test
    void convertToEntityAttributeThrowsException() {
        String invalidString = "{";
        BgosException bgosException = assertThrows(BgosException.class, () -> bankGuaranteeRequestTransformer.convertToEntityAttribute(invalidString));
        assertThat(bgosException).isNotNull();
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }


}
