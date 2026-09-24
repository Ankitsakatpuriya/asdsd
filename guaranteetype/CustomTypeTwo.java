package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CustomTypeTwo implements BaseGuaranteeType {
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        log.info("CustomTypeTwo [validate] validating Custom Type Two Bond  all fields ");
        context.disableDefaultConstraintViolation();

        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            return false;
        }
        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            return false;
        }
        return true;
    }
}
