package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

import static com.ing.bankguarantees.utils.ConstantUtils.WOODS_PROMISE_SALE_DATE_EXTRA_MONTHS;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class WoodsWalloniaStandardPromise implements BaseGuaranteeType {

    private LocalDate saleDate;
    private String salePlace;
    private RegionCode region;
    private LocalDate promiseEndDate;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Wallonia Standard Promise [validate] validating Standard Promise all fields");
        if (StringUtils.isEmpty(salePlace)) {
            context.buildConstraintViolationWithTemplate("salePlace must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(saleDate)) {
            context.buildConstraintViolationWithTemplate("saleDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (region != RegionCode.WALLONIA) {
            context.buildConstraintViolationWithTemplate("region must be Wallonia").addConstraintViolation();
            validationResult = false;
        }

        if (ObjectUtils.isEmpty(promiseEndDate)) {
            context.buildConstraintViolationWithTemplate("promiseEndDate must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            return false;
        }

        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            return false;
        }

        if (region == RegionCode.WALLONIA && ObjectUtils.isNotEmpty(promiseEndDate) && ObjectUtils.isNotEmpty(saleDate) && !promiseEndDate.isEqual(saleDate.plusMonths(WOODS_PROMISE_SALE_DATE_EXTRA_MONTHS))) {
            context.buildConstraintViolationWithTemplate("For Wallonia region, promiseEndDate should be sale date plus four months").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }
}
