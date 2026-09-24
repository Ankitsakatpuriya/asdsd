package com.ing.bankguarantees.models.guaranteetype;


import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceBond implements BaseGuaranteeType {

    private String contractDescription;
    private String referenceNumber;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private LocalDate maturityDate;
    private LocalDate immediateMaturityDate;
    private LocalDate finalMaturityDate;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("PublicContract [validate] validating Performance Bond all fields ");
        if (StringUtils.isEmpty(contractDescription)) {
            context.buildConstraintViolationWithTemplate("contractDescription must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (StringUtils.isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (StringUtils.isEmpty(referenceNumber)) {
            context.buildConstraintViolationWithTemplate("referenceNumber must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (bankGuaranteeEndType == BankGuaranteeEndType.SPECIFIED && ObjectUtils.isEmpty(maturityDate)) {
            context.buildConstraintViolationWithTemplate("maturityDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (bankGuaranteeEndType == BankGuaranteeEndType.ACCEPTANCE_WITH_EXPIRY_DATE && ObjectUtils.isEmpty(finalMaturityDate)) {
            context.buildConstraintViolationWithTemplate("finalMaturityDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
