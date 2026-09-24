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

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class AdvancePayment implements BaseGuaranteeType {
    private String contractDescription;
    private String referenceNumber;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private LocalDate maturityDate;
    private String advancePaymentIBAN;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("AdvancePayment [validate] validating Advance Payment  all fields ");
        if (isEmpty(contractDescription)) {
            context.buildConstraintViolationWithTemplate("contractDescription must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(referenceNumber)) {
            context.buildConstraintViolationWithTemplate("referenceNumber must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (bankGuaranteeEndType == BankGuaranteeEndType.SPECIFIED && ObjectUtils.isEmpty(maturityDate)) {
            context.buildConstraintViolationWithTemplate("maturityDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(advancePaymentIBAN)) {
            context.buildConstraintViolationWithTemplate("Bank Account number  must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
