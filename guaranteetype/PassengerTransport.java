package com.ing.bankguarantees.models.guaranteetype;


import com.ing.bankguarantees.models.enums.BankGuaranteeEndType;
import com.ing.bankguarantees.models.enums.FodGuaranteeType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PassengerTransport implements BaseGuaranteeType {

    private String licenseNumber;
    private String bankGuaranteeId;
    private FodGuaranteeType fodGuarantee;
    private Integer noOfVehicle;
    private String bankGuaranteeEnd;
    private BankGuaranteeEndType bankGuaranteeEndType;

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        boolean validationResult = true;
        context.disableDefaultConstraintViolation();

        log.info("GoodsTransports [validate] validating Goods Transports all fields ");

        if (ObjectUtils.isEmpty(bankGuaranteeEnd)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEnd must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(bankGuaranteeEndType)) {
            context.buildConstraintViolationWithTemplate("bankGuaranteeEndType must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        if (ObjectUtils.isEmpty(noOfVehicle)) {
            context.buildConstraintViolationWithTemplate("noOfVehicle must not be empty or null").addConstraintViolation();
            validationResult = false;
        }
        return validationResult;
    }
}
