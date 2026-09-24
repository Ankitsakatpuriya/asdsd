package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CustomTypeOne implements BaseGuaranteeType {
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private String representing;
    private Integer amountPercentage;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("CustomTypeOne [validate] validating Custom Type one  all fields ");
        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (isEmpty(representing)) {
            context.buildConstraintViolationWithTemplate("representing must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(amountPercentage)) {
            context.buildConstraintViolationWithTemplate("amountPercentage must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
