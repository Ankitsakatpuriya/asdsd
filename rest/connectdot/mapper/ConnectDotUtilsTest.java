package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.BeneficiaryData;
import com.ing.bankguarantees.models.enums.BeneficiaryType;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BeneficiaryDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.util.MockHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ing.bankguarantees.models.enums.BankGuaranteeCode.WOODS_PROM_B;
import static com.ing.bankguarantees.utils.CommonUtils.formatReferenceIdNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ConnectDotUtilsTest {


    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";

    @Mock
    private CountryDetailService countryDetailService;

    @InjectMocks
    private ConnectDotUtils connectDotUtils;


    @BeforeEach
    void setup() {
        lenient().when(countryDetailService.getCountryNameByCode(any(), any())).thenReturn("Belgium");
    }

    @Test
    void checkPrepareBeneficiaryPositive() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData);
        BeneficiaryDocumentPayload beneficiaryDocumentPayload = connectDotUtils.prepareBeneficiary(connectDotInput);
        assertThat(beneficiaryDocumentPayload).isNotNull();
        assertThat(beneficiaryDocumentPayload.getId()).isEmpty();
        assertThat(beneficiaryDocumentPayload.getCompanyName()).isEqualTo(beneficiary.getOrganisationName().getFullName());
    }

    @Test
    void checkPrepareBeneficiaryPrivateIndividualPositive() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        beneficiary.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        BeneficiaryData.BeneficiaryPrivateIndividualData privateIndividual = beneficiary.getPrivateIndividual();
        privateIndividual.setBelgiumCitizen(true);
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData);
        BeneficiaryDocumentPayload beneficiaryDocumentPayload = connectDotUtils.prepareBeneficiary(connectDotInput);
        assertThat(beneficiaryDocumentPayload).isNotNull();
        assertThat(beneficiaryDocumentPayload.getId()).isEqualTo(formatReferenceIdNumber(privateIndividual.getPrimaryIdentificationReference()));
        assertThat(beneficiaryDocumentPayload.getCompanyName()).isEqualTo(privateIndividual.getPrimaryBeneficiaryName());
        assertThat(beneficiaryDocumentPayload.getSecondaryBeneficiaryName()).isEqualTo(privateIndividual.getSecondaryBeneficiaryName());
    }

    @Test
    void checkPrepareBeneficiaryPrivateIndividualOutOfBelgiumPositive() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        beneficiary.setBeneficiaryType(BeneficiaryType.PRIVATE_INDIVIDUAL);
        BeneficiaryData.BeneficiaryPrivateIndividualData privateIndividual = beneficiary.getPrivateIndividual();
        privateIndividual.setBelgiumCitizen(false);
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData);
        BeneficiaryDocumentPayload beneficiaryDocumentPayload = connectDotUtils.prepareBeneficiary(connectDotInput);
        assertThat(beneficiaryDocumentPayload).isNotNull();

        assertThat(beneficiaryDocumentPayload.getId()).isEqualTo(formatReferenceIdNumber(privateIndividual.getPrimaryIdentificationReference()));
        assertThat(beneficiaryDocumentPayload.getSecondaryBeneficiaryId()).isEqualTo(formatReferenceIdNumber(privateIndividual.getSecondaryIdentificationReference()));
    }

    @Test
    void checkPrepareBeneficiaryForBlankPromisePositive() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        bankGuaranteeRequestData.getGuaranteeDetails().setBgCode(WOODS_PROM_B);
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData);
        BeneficiaryDocumentPayload beneficiaryDocumentPayload = connectDotUtils.prepareBeneficiary(connectDotInput);
        assertThat(beneficiaryDocumentPayload).isNull();
    }

    private String getPrivateIndividualId(String primaryNumber, String secondaryNumber) {
        return StringUtils.isNotEmpty(primaryNumber) && StringUtils.isNotEmpty(secondaryNumber)
                ? String.join(",", formatReferenceIdNumber(primaryNumber), formatReferenceIdNumber(secondaryNumber))
                : primaryNumber;
    }
}