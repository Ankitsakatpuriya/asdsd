package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.RegionCode;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class WoodsBaseGuaranteeType implements BaseGuaranteeType {

    private RegionCode region;
    private LocalDate saleDate;
    private String salePlace;
    private String lotsDescription;
    private boolean replacePromise;
    private String promiseIds;
    private BigDecimal cash;
    private BigDecimal firstTransactionAmt;
    private LocalDate firstDeadline;
    private BigDecimal secondTransactionAmt;
    private LocalDate secondDeadline;
    private BigDecimal thirdTransactionAmt;
    private LocalDate thirdDeadline;
    private BigDecimal fourthTransactionAmt;
    private LocalDate fourthDeadline;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private String bankGuaranteeEnd;

    @Override
    public boolean validate(ConstraintValidatorContext context) {

        boolean validationResult = true;

        context.disableDefaultConstraintViolation();
        log.info("WoodsBase [validate] validating WoodsBase base fields ");

        if (replacePromise && ObjectUtils.isEmpty(promiseIds)) {
            context.buildConstraintViolationWithTemplate("promise Id/Ids must not be empty or null").addConstraintViolation();
            validationResult = false;
        }

        if (replacePromise && ObjectUtils.isNotEmpty(promiseIds) && promiseIds.length() < 16) {
            context.buildConstraintViolationWithTemplate("promise Id must have 16 character length").addConstraintViolation();
            validationResult = false;
        }

        if (replacePromise && ObjectUtils.isNotEmpty(promiseIds) && promiseIds.length() > 16) {
            String promiseId = promiseIds.replaceAll("\\s+", "").split(",")[0];
            if (promiseId.length() != 16) {
                context.buildConstraintViolationWithTemplate("promise Id/Ids must have 16 character length").addConstraintViolation();
                validationResult = false;
            }
        }

        return validationResult;
    }

    public LocalDate getLatestDeadline() {
        return Stream.of(firstDeadline, secondDeadline, thirdDeadline, fourthDeadline)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    public BigDecimal getLastTrancheAmount() {
        return Stream.of(firstTransactionAmt, secondTransactionAmt, thirdTransactionAmt, fourthTransactionAmt)
                .filter(Objects::nonNull)
                .reduce((a, b) -> b)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalAmountTranchesPlusCash() {
        return Stream.of(cash, firstTransactionAmt, secondTransactionAmt, thirdTransactionAmt, fourthTransactionAmt)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
