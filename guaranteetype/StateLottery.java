package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateLottery implements BaseGuaranteeType {
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private LocalDate dateOfAgreeInPrinc;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("StateLottery [validate] validating State Lottery all fields ");

        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(dateOfAgreeInPrinc)) {
            context.buildConstraintViolationWithTemplate("dateOfAgreeInPrinc must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }
}
