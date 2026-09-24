package com.ing.bankguarantees.database.transformer;

import tools.jackson.databind.ObjectMapper;
import com.ing.bankguarantees.configuration.JacksonConfiguration;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;


@Slf4j
@Converter
public class BankGuaranteeRequestTransformer implements AttributeConverter<BankGuaranteeRequestData, String>, Serializable {

    private static final ObjectMapper OBJECT_MAPPER = JacksonConfiguration.createDefaultMapper();

    @Override
    public String convertToDatabaseColumn(BankGuaranteeRequestData bankGuaranteeRequestLob) {
        try {
            return OBJECT_MAPPER.writeValueAsString(bankGuaranteeRequestLob);
        } catch (Exception ex) {
            log.error("Error while serializing Bank guarantee request  details:: {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    @Override
    public BankGuaranteeRequestData convertToEntityAttribute(String bankGuaranteeRequestString) {
        try {
            return OBJECT_MAPPER.readValue(bankGuaranteeRequestString, BankGuaranteeRequestData.class);
        } catch (Exception ex) {
            log.error("Error while deserializing bank guarantee request from db:: {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }
}
