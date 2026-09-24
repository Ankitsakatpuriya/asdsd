package com.ing.bankguarantees.models.guaranteetype;

import jakarta.validation.ConstraintValidatorContext;

public interface BaseGuaranteeType {

    boolean validate(ConstraintValidatorContext context);

}
