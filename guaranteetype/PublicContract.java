package com.ing.bankguarantees.models.guaranteetype;


import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PublicContract implements BaseGuaranteeType {


    private Boolean tenderSpecification;
    private String referenceNumber;
    private String title;
    private BigDecimal totalAmount;
    private String totalAmtCurrency;
    private LocalDate grantDate;
    private String expiryDate;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("PublicContract [validate] validating Public Contract all fields ");

        if (ObjectUtils.isEmpty(tenderSpecification)) {
            context.buildConstraintViolationWithTemplate("tenderSpecification must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(referenceNumber)) {
            context.buildConstraintViolationWithTemplate("referenceNumber must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(title)) {
            context.buildConstraintViolationWithTemplate("title must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(totalAmount)) {
            context.buildConstraintViolationWithTemplate("totalAmount must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(grantDate)) {
            context.buildConstraintViolationWithTemplate("grantDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(expiryDate)) {
            context.buildConstraintViolationWithTemplate("expiryDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }
}
