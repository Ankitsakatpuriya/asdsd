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

import static com.ing.bankguarantees.utils.ConstantUtils.ABSTRACT_PROMISE_SYSTEM_DATE_EXTRA_MONTHS;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PromiseAbstract implements BaseGuaranteeType {

    private String contractDescription;
    private String referenceNumber;
    private LocalDate promiseEndDate;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Promise Abstract [validate] validating Promise Abstract all fields ");

        if (ObjectUtils.isEmpty(promiseEndDate)) {
            context.buildConstraintViolationWithTemplate("promiseEndDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(referenceNumber)) {
            context.buildConstraintViolationWithTemplate("referenceNumber must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(contractDescription)) {
            context.buildConstraintViolationWithTemplate("contractDescription must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(promiseEndDate) && !promiseEndDate.equals(LocalDate.now().plusMonths(ABSTRACT_PROMISE_SYSTEM_DATE_EXTRA_MONTHS))) {
            context.buildConstraintViolationWithTemplate("promiseEndDate should be system date plus six months").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
