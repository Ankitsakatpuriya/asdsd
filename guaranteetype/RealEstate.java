package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealEstate implements BaseGuaranteeType {

    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        log.info("RealEstate [validate] validating Real Estate all fields ");
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
