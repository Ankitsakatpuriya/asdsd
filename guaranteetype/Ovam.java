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
public class Ovam implements BaseGuaranteeType {

    private String contractDescription;
    private String referenceNumber;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private LocalDate wasteTransportStartDate;
    private LocalDate wasteTransportEndDate;
    private LocalDate contractDueDate;
    private LocalDate contractValidityEndDate;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Ovam [validate] validating Ovam  all fields ");
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
        if (ObjectUtils.isEmpty(wasteTransportStartDate)) {
            context.buildConstraintViolationWithTemplate("wasteTransportStartDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(wasteTransportEndDate)) {
            context.buildConstraintViolationWithTemplate("wasteTransportEndDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(contractDueDate)) {
            context.buildConstraintViolationWithTemplate("contractDueDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(contractValidityEndDate)) {
            context.buildConstraintViolationWithTemplate("contractValidityEndDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }
}
