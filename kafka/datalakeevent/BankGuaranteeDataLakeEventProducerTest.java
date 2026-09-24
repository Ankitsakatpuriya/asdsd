package com.ing.bankguarantees.remote.kafka.datalakeevent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ing.bankguarantees.avro.BankGuaranteesDataLakeEvent;
import com.ing.bankguarantees.avro.BeneficiaryData;
import com.ing.bankguarantees.avro.InstructingPartyData;
import com.ing.bankguarantees.configuration.JacksonConfiguration;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.kafka.datalakeevent.model.BgRequestDataLakeEventDto;
import com.ing.bankguarantees.remote.kafka.datalakeevent.producer.BankGuaranteeDataLakeEventProducer;
import com.ing.bankguarantees.remote.kafka.datalakeevent.producer.DataLakeEventGateway;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BankGuaranteeDataLakeEventProducerTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String MASTER_REFERENCE_ID = "BEBTFSBGR6000012";
    private static final String PEGA_CASE_ID = "ING-WB-TFS-WORK TFS-BG-5363";

    @Mock
    private DataLakeEventGateway dataLakeEventGateway;

    private BankGuaranteeDataLakeEventProducer bankGuaranteeDataLakeEventProducer;

    @BeforeEach
    void setup() {

        bankGuaranteeDataLakeEventProducer = new BankGuaranteeDataLakeEventProducer(dataLakeEventGateway);
    }

    @ParameterizedTest
    @MethodSource("bgCodes")
    void notifyPositive(BankGuaranteeCode bankGuaranteeCode) throws JsonProcessingException {
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = MockHelper.getGuaranteeDetails(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        bankGuaranteeRequestData.setStpResultDataSet(MockHelper.createStpResultDataset(BG_STP_RESULT_FILE));
        bankGuaranteeRequest.setMasterReferenceNumber(MASTER_REFERENCE_ID);
        bankGuaranteeRequest.setPegaCaseId(PEGA_CASE_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        BgRequestDataLakeEventDto bgRequestDataLakeDto = MockHelper.getBgRequestDataLakeDto(bankGuaranteeRequest);
        when(dataLakeEventGateway.send(any())).thenReturn(CompletableFuture.completedFuture(true));
        ArgumentCaptor<BankGuaranteesDataLakeEvent> argumentCaptor = ArgumentCaptor.forClass(BankGuaranteesDataLakeEvent.class);

        Boolean result = bankGuaranteeDataLakeEventProducer.notify(bgRequestDataLakeDto).join();

        verify(dataLakeEventGateway, times(1)).send(argumentCaptor.capture());
        BankGuaranteesDataLakeEvent expectedArgument = argumentCaptor.getValue();
        assertThat(result).isNotNull().isTrue();
        assertInstructingParty(expectedArgument, bankGuaranteeRequestData);
        assertBeneficiary(expectedArgument, bankGuaranteeRequestData);
        assertGuaranteeDetails(expectedArgument, bankGuaranteeRequestData);
    }


    @Test
    void notifyError() {

        try (MockedStatic<InetAddress> inetAddressMockedStatic = mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException());
            BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
            BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
            StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
            bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
            bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
            BgRequestDataLakeEventDto bgRequestDataLakeDto = MockHelper.getBgRequestDataLakeDto(bankGuaranteeRequest);
            BgosException exception = assertThrows(BgosException.class, () -> bankGuaranteeDataLakeEventProducer.notify(bgRequestDataLakeDto));
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNotNull();
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
        }
    }

    private void assertInstructingParty(BankGuaranteesDataLakeEvent expectedArgument, BankGuaranteeRequestData bankGuaranteeRequestData) {
        InstructingPartyData avroInstructingParty = expectedArgument.getBody().getBgRequest().getInstructingParty();
        com.ing.bankguarantees.models.domain.InstructingPartyData instructingParty = bankGuaranteeRequestData.getInstructingParty();
        assertThat(avroInstructingParty.getOrganisation().getLegalEntityId()).isEqualTo(instructingParty.getOrganisation().getLegalEntityId());
        assertThat(avroInstructingParty.getOrganisation().getCinNumber()).isEqualTo(instructingParty.getOrganisation().getCinNumber());
        assertThat(avroInstructingParty.getOrganisation().getDigitalAddresses().size()).isEqualTo(instructingParty.getOrganisation().getDigitalAddresses().size());
        assertThat(avroInstructingParty.getOrganisation().getInternalIdentifiers().size()).isEqualTo(instructingParty.getOrganisation().getInternalIdentifiers().size());
        assertThat(avroInstructingParty.getOrganisation().getOrganisationName().getFullName()).isEqualTo(instructingParty.getOrganisation().getOrganisationName().getFullName());
        assertThat(avroInstructingParty.getIndividual().getInternalIdentifiers().size()).isEqualTo(instructingParty.getIndividual().getInternalIdentifiers().size());
        assertThat(avroInstructingParty.getIndividual().getDigitalAddresses().size()).isEqualTo(instructingParty.getIndividual().getDigitalAddresses().size());
        assertThat(avroInstructingParty.getIndividual().getIndividualName().getFullName()).isEqualTo(instructingParty.getIndividual().getIndividualName().getFullName());
    }

    private void assertBeneficiary(BankGuaranteesDataLakeEvent expectedArgument, BankGuaranteeRequestData bankGuaranteeRequestData) {
        BeneficiaryData avroBeneficiary = expectedArgument.getBody().getBgRequest().getBeneficiary();
        com.ing.bankguarantees.models.domain.BeneficiaryData beneficiary = bankGuaranteeRequestData.getBeneficiary();
        assertThat(avroBeneficiary.getCinNumber()).isEqualTo(beneficiary.getCinNumber());
        assertThat(avroBeneficiary.getBeneficiaryType()).isEqualTo(beneficiary.getBeneficiaryType().name());
        assertThat(avroBeneficiary.getOrganisationName().getFullName()).isEqualTo(beneficiary.getOrganisationName().getFullName());
        assertThat(avroBeneficiary.getEmailAddress().getEmailIdInformation()).isEqualTo(beneficiary.getEmailAddress().getEmailIdInformation());
        assertThat(avroBeneficiary.getPrivateIndividual().getBelgiumCitizen()).isEqualTo(beneficiary.getPrivateIndividual().isBelgiumCitizen());
        assertThat(avroBeneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob()).isEqualTo(beneficiary.getPrivateIndividual().getPrimaryBeneficiaryDob());
        assertThat(avroBeneficiary.getPrivateIndividual().getPrimaryBeneficiaryName()).isEqualTo(beneficiary.getPrivateIndividual().getPrimaryBeneficiaryName());
        assertThat(avroBeneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob()).isEqualTo(beneficiary.getPrivateIndividual().getSecondaryBeneficiaryDob());
        assertThat(avroBeneficiary.getPrivateIndividual().getPrimaryIdentificationReference()).isEqualTo(beneficiary.getPrivateIndividual().getSecondaryIdentificationReference());
        assertThat(avroBeneficiary.getPrivateIndividual().getSecondaryIdentificationReference()).isEqualTo(beneficiary.getPrivateIndividual().getSecondaryIdentificationReference());
    }

    private void assertGuaranteeDetails(BankGuaranteesDataLakeEvent expectedArgument, BankGuaranteeRequestData bankGuaranteeRequestData) throws JsonProcessingException {
        com.ing.bankguarantees.avro.GuaranteeDetailsData avroGuaranteeDetails = expectedArgument.getBody().getBgRequest().getGuaranteeDetails();
        GuaranteeDetailsData<?> guaranteeDetails = bankGuaranteeRequestData.getGuaranteeDetails();
        assertThat(avroGuaranteeDetails.getBgAmount()).isEqualTo(guaranteeDetails.getBgAmount());
        assertThat(avroGuaranteeDetails.getBgCurrency()).isEqualTo(guaranteeDetails.getBgCurrency());
        assertThat(avroGuaranteeDetails.getBgLanguage()).isEqualTo(guaranteeDetails.getBgLanguage().name());
        assertThat(avroGuaranteeDetails.getBgCode().name()).isEqualTo(guaranteeDetails.getBgCode().name());
    }


    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }

}
