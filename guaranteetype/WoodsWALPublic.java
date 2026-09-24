package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.CoverType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;

@Slf4j
@NoArgsConstructor
@Data
@SuperBuilder
@AllArgsConstructor
public class WoodsWALPublic extends WoodsBaseGuaranteeType {

    private BigDecimal salePrice;
    private CoverType coverType;
    private BigDecimal residualAmount;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = super.validate(context);

        log.info("WoodsWALPublic [validate] validating WoodsWALPublic all fields ");
        context.disableDefaultConstraintViolation();

        if (getRegion() != RegionCode.WALLONIA) {
            context.buildConstraintViolationWithTemplate("region must be Wallonia").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(salePrice)) {
            context.buildConstraintViolationWithTemplate("price of sale must not be empty").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(residualAmount)) {
            context.buildConstraintViolationWithTemplate("residual amount must not be empty").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(getBankGuaranteeEndType()) && getBankGuaranteeEndType() != BankGuaranteeEndType.UNSPECIFIED) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must be unspecified").addConstraintViolation();
            validationResult = false;
        }
        if (coverType != null && coverType != CoverType.CASH) {
            context.buildConstraintViolationWithTemplate("Cover type should be chosen as cash").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
