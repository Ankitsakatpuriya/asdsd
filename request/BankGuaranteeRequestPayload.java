package com.ing.bankguarantees.models.request;

import com.ing.bankguarantees.validation.annotation.ValidBankGuaranteeRequestPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Locale;


@Data
@Builder(toBuilder = true)
@Validated
@NoArgsConstructor
@AllArgsConstructor
@ValidBankGuaranteeRequestPayload
public class BankGuaranteeRequestPayload {

    private boolean issueToAnotherParty;

    private Locale translationLanguage;

    @Valid
    @NotNull
    private InstructingPartyPayload instructingParty;

    @Valid
    private ApplicantPayload applicant;

    @Valid
    private BeneficiaryPayload beneficiary;

    @NotNull
    @Valid
    private FinancialInformationPayload financialInformation;

    @NotNull
    private GuaranteeDetailsPayload<?> guaranteeDetails;

    @NotNull
    private DeliveryInformationPayload deliveryInformation;

    @NotNull
    private AlerDetailsPayload alerDetails;


    private CustomDocumentDetailPayload customDocumentDetails;
}
