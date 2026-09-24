package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import static org.apache.commons.lang3.StringUtils.isEmpty;


@Slf4j
@NoArgsConstructor
@Data
@SuperBuilder
public class WoodsVLAPublic extends WoodsBaseGuaranteeType {

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = super.validate(context);

        log.info("WoodsVLAPublic [validate] validating WoodsVLAPublic all fields");
        context.disableDefaultConstraintViolation();

        if (getRegion() != RegionCode.FLANDERS) {
            context.buildConstraintViolationWithTemplate("region must be Flanders").addConstraintViolation();
            validationResult = false;
        }

        if (isEmpty(getBankGuaranteeEnd())) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (getBankGuaranteeEndType() == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (getBankGuaranteeEndType() != null && getBankGuaranteeEndType() != BankGuaranteeEndType.UNSPECIFIED) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must be unspecified").addConstraintViolation();
            validationResult = false;
        }

        return validationResult;
    }
}
