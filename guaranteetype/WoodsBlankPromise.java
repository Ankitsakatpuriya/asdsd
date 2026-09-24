package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;

import static com.ing.bankguarantees.utils.ConstantUtils.WOODS_PROMISE_SYSTEM_DATE_EXTRA_MONTHS;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class WoodsBlankPromise implements BaseGuaranteeType {

    private String lotsDescription;
    private LocalDate promiseEndDate;
    private RegionCode region;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Blank Promise [validate] validating Blank Promise all fields ");

        if (region != RegionCode.WALLONIA) {
            context.buildConstraintViolationWithTemplate("region must be wallonia").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(promiseEndDate)) {
            context.buildConstraintViolationWithTemplate("promiseEndDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            return false;
        }

        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            return false;
        }

        if (ObjectUtils.isNotEmpty(promiseEndDate) && !promiseEndDate.equals(LocalDate.now().plusMonths(WOODS_PROMISE_SYSTEM_DATE_EXTRA_MONTHS))) {
            context.buildConstraintViolationWithTemplate("promiseEndDate should be system date plus eight months").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
