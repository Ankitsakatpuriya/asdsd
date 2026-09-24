package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.ing.bankguarantees.utils.ConstantUtils.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@NoArgsConstructor
@Data
@SuperBuilder
public class WoodsPrivate extends WoodsBaseGuaranteeType {

    private LocalDate maturityDate;
    private BigDecimal salePrice;
    private BigDecimal residualAmount;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = super.validate(context);

        log.info("WoodsPrivate [validate] validating WoodsPrivate all fields");
        context.disableDefaultConstraintViolation();

        if (isEmpty(getBankGuaranteeEnd())) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (getBankGuaranteeEndType() == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(salePrice)) {
            context.buildConstraintViolationWithTemplate("salePrice  must not be empty or null").addConstraintViolation();
            validationResult = false;
        }


        if (ObjectUtils.isEmpty(maturityDate)) {
            context.buildConstraintViolationWithTemplate("maturity date  must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isNotEmpty(residualAmount) && residualAmount.compareTo(BigDecimal.ZERO) > 0 && residualAmount.compareTo(getLastTrancheAmount()) > 0) {
            context.buildConstraintViolationWithTemplate("Residual amount should be lower then the last tranche amount entered").addConstraintViolation();
            validationResult = false;
        }

        if (getRegion() == RegionCode.WALLONIA && ObjectUtils.isNotEmpty(maturityDate) && getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED) {

            if (ObjectUtils.isNotEmpty(residualAmount) && !getLatestDeadline().plusDays(WOODS_BG_WALLONIA_REGION_With_RESEDUAL_AMT_END_DATE).equals(maturityDate)) {
                context.buildConstraintViolationWithTemplate("maturityDate must be last deadline plus 365 days").addConstraintViolation();
                validationResult = false;
            }

            if (ObjectUtils.isEmpty(residualAmount) && !getLatestDeadline().plusDays(WOODS_BG_WALLONIA_REGION_PROMISE_END_DATE).equals(maturityDate)) {
                context.buildConstraintViolationWithTemplate("maturityDate must be last deadline plus 45 days").addConstraintViolation();
                validationResult = false;
            }

        }

        if (getRegion() == RegionCode.FRANCE && ObjectUtils.isNotEmpty(maturityDate) && getBankGuaranteeEndType() == BankGuaranteeEndType.SPECIFIED) {
            if (ObjectUtils.isNotEmpty(residualAmount) && !getLatestDeadline().plusDays(WOODS_BG_WALLONIA_REGION_With_RESEDUAL_AMT_END_DATE).equals(maturityDate)) {
                context.buildConstraintViolationWithTemplate("maturityDate must be last deadline plus 365 days").addConstraintViolation();
                validationResult = false;
            }
        }

        return validationResult;
    }
}
