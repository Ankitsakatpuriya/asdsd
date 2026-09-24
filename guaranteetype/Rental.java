package com.ing.bankguarantees.models.guaranteetype;

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
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental implements BaseGuaranteeType {

    private String expiryDate;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private LocalDate dateOfSignature;
    private LocalDate contractEndDate;
    private LocalDate rentalEndDate;
    private String referenceNumber;
    private String gracePeriod;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Rental [validate] validating Rental all fields ");
        if (isEmpty(street)) {
            context.buildConstraintViolationWithTemplate("street must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(postalCode)) {
            context.buildConstraintViolationWithTemplate("postalCode must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(dateOfSignature)) {
            context.buildConstraintViolationWithTemplate("dateOfSignature must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(city)) {
            context.buildConstraintViolationWithTemplate("city must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (isEmpty(country)) {
            context.buildConstraintViolationWithTemplate("country  must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(rentalEndDate)) {
            context.buildConstraintViolationWithTemplate("rentalEndDate  must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(contractEndDate)) {
            context.buildConstraintViolationWithTemplate("contractEndDate must not be  null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(dateOfSignature) && ObjectUtils.isNotEmpty(contractEndDate) && dateOfSignature.isAfter(contractEndDate)) {
            context.buildConstraintViolationWithTemplate("dateOfSignature must  be before the contractEndDate").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(contractEndDate) && contractEndDate.isBefore(LocalDate.now())) {
            context.buildConstraintViolationWithTemplate("contractEndDate must not be the past date.").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(rentalEndDate) && !rentalEndDate.isEqual(contractEndDate.plusDays(Integer.parseInt(gracePeriod)))) {
            context.buildConstraintViolationWithTemplate("rentalEndDate must be equal to the contractEndDate plus grace period").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
