package com.ing.bankguarantees.models.guaranteetype;

import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Dck implements BaseGuaranteeType {

    private String contractDescription;
    private BankGuaranteeEndType bankGuaranteeEndType;
    private String bankGuaranteeEnd;
    private BuildingAddress buildingAddress;


    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("Dck [validate] validating Dck all fields ");
        if (StringUtils.isBlank(contractDescription)) {
            context.buildConstraintViolationWithTemplate("contractDescription must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (bankGuaranteeEndType == null) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (buildingAddress == null) {
            context.buildConstraintViolationWithTemplate("BuildingAddress must not be empty or null").addConstraintViolation();
            validationResult = false;
        }else{
            if (StringUtils.isBlank(buildingAddress.city)) {
                context.buildConstraintViolationWithTemplate("City name must not be empty or null").addConstraintViolation();
                validationResult = false;
            }
            if (StringUtils.isBlank(buildingAddress.postalCode)) {
                context.buildConstraintViolationWithTemplate("Postal code must not be empty or null").addConstraintViolation();
                validationResult = false;
            }
            if (StringUtils.isBlank(buildingAddress.street)) {
                context.buildConstraintViolationWithTemplate("Street number must not be empty or null").addConstraintViolation();
                validationResult = false;
            }
        }

        if (StringUtils.isBlank(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildingAddress {
        private String street;
        private String postalCode;
        private String city;
    }
}
