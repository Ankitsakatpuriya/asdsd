package com.ing.bankguarantees.models.domain;

import com.ing.bankguarantees.models.enums.FundReservedBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankGuaranteeRequestData {

    private String requestId;

    private Locale translationLanguage;

    private boolean issueToAnotherParty;

    private boolean signingAllowed;

    private boolean signed;

    private boolean wbCustomer;

    private boolean stp;

    private boolean amountCapExceed;

    private String failureRemarks;

    private LocalDate signExpiryDate;

    private InstructingPartyData instructingParty;

    private ApplicantData applicant;

    private BeneficiaryData beneficiary;

    private FinancialInformationData financialInformation;

    private GuaranteeDetailsData<?> guaranteeDetails;

    private DeliveryInformationData deliveryInformation;

    private AlerDetailsData alerDetails;

    private List<LegalRepresentativeData> legalRepresentatives;

    private DossierData dossierInformation;

    private String referenceNumber;

    private StpResultDataSet stpResultDataSet;

    private String darId;

    private FundReservedBy fundReservedBy;

    private CustomDocumentDetailData customDocumentDetails;

}
