package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.LegalRepresentativeData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.BaseDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.payload.ContractDocumentPayload;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.service.referencedata.CountryDetailService;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.util.TestConstants;
import com.ing.bankguarantees.utils.CommonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractDocumentMapperTest {

    DocumentType documentType = DocumentType.BG_DRAFT;

    @Mock
    private CurrencyDetailService currencyService;
    @Mock
    private ConnectDotUtils connectDotUtils;
    @Mock
    private ConnectDotProperties connectDotProperties;
    @Mock
    private CountryDetailService countryService;

    @InjectMocks
    private ContractDocumentMapper contractDocumentMapper;

    private static final String MINIMUM_PER_RECORD_NUMBER = "250";

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";


    @ParameterizedTest
    @MethodSource("provideGuaranteeTypes")
    void shouldMapToContractDocumentPayload(BankGuaranteeCode code, BaseGuaranteeType guaranteeObject) {

        // Arrange
        GuaranteeDetailsData<Object> guaranteeDetailsData = MockHelper.getGuaranteeDetailsData(code, guaranteeObject);
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.getBankGuaranteeRequestData(guaranteeDetailsData, BankGuaranteeRecipient.ME, true);
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData, documentType);

        when(connectDotProperties.getMinimumPerRecord()).thenReturn(MINIMUM_PER_RECORD_NUMBER);

        // Act
        BaseDocumentPayload result = contractDocumentMapper.mapToContractPayload(connectDotInput);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReferenceNumber()).isEqualTo(TestConstants.REFERENCE_NUMBER);
    }

    @Test
    void checkRepresentedBy() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        ConnectDotInput connectDotInput = MockHelper.getConnectDotInput(bankGuaranteeRequestData, documentType);
        when(connectDotProperties.getMinimumPerRecord()).thenReturn(MINIMUM_PER_RECORD_NUMBER);
        ContractDocumentPayload result = (ContractDocumentPayload) contractDocumentMapper.mapToContractPayload(connectDotInput);
        List<LegalRepresentativeData> selectedSigner = CommonUtils.getSelectedSigner(bankGuaranteeRequestData.getLegalRepresentatives());
        String firstName = selectedSigner.stream().filter(LegalRepresentativeData::isFirstSigner).map(LegalRepresentativeData::getFullName).findFirst().orElse(null);
        String secondName = selectedSigner.stream().filter(signer -> !signer.isFirstSigner()).map(LegalRepresentativeData::getFullName).findFirst().orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getRepresentedBy().getLegalRep1()).isEqualTo(firstName);
        assertThat(result.getRepresentedBy().getLegalRep2()).isEqualTo(secondName);
    }


    private static Stream<Arguments> provideGuaranteeTypes() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.STATE_LOTTERY, MockHelper.getStateLotteryDetails()),
                Arguments.of(BankGuaranteeCode.RENTAL, MockHelper.getRentalDetails()),
                Arguments.of(BankGuaranteeCode.PUBLIC_CONTRACT, MockHelper.getPublicContractDetails()),
                Arguments.of(BankGuaranteeCode.OVAM, MockHelper.getOvamDetails()),
                Arguments.of(BankGuaranteeCode.CUSTOM_1, MockHelper.getCustomOneDetails()),
                Arguments.of(BankGuaranteeCode.REAL_ESTATE, MockHelper.getRealEstateDetails()),
                Arguments.of(BankGuaranteeCode.CUSTOM_2, MockHelper.getCustomTwoDetails()),
                Arguments.of(BankGuaranteeCode.CUSTOM_5, MockHelper.getCustomFiveDetails()),
                Arguments.of(BankGuaranteeCode.CUSTOM_4, MockHelper.getCustomFourDetails()),
                Arguments.of(BankGuaranteeCode.BID_BOND, MockHelper.getBidBond()),
                Arguments.of(BankGuaranteeCode.ADVANCE_PAYMENT, MockHelper.getAdvancePaymentDetails()),
                Arguments.of(BankGuaranteeCode.PAYMENT_GUARANTEE, MockHelper.getPaymentDetails()),
                Arguments.of(BankGuaranteeCode.PERFORMANCE_BOND, MockHelper.getPerformanceBondDetails()),
                Arguments.of(BankGuaranteeCode.MONEY_RETENTION_BOND, MockHelper.getMoneyRetentionBond()),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_A, MockHelper.getStandardPromiseDetails()),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_B, MockHelper.getBlankPromiseDetails()),
                Arguments.of(BankGuaranteeCode.GOODS_TRANSPORT, MockHelper.getGoodsTransport()),
                Arguments.of(BankGuaranteeCode.PASSENGER_TRANSPORT, MockHelper.getPassengerTransport()),
                Arguments.of(BankGuaranteeCode.OPERATORS_TRANSPORT, MockHelper.getTransportOperator())

        );
    }
}
