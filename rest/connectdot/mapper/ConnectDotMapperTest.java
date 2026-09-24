package com.ing.bankguarantees.remote.rest.connectdot.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.BankGuaranteeRecipient;
import com.ing.bankguarantees.models.enums.DocumentType;
import com.ing.bankguarantees.models.guaranteetype.BidBond;
import com.ing.bankguarantees.remote.rest.connectdot.ConnectDotProperties;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotInput;
import com.ing.bankguarantees.remote.rest.connectdot.model.request.ConnectDotRequest;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.ing.bankguarantees.util.MockHelper.getBankGuaranteeRequestData;
import static com.ing.bankguarantees.util.MockHelper.getConnectDotInput;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConnectDotMapperTest {

    @Mock
    private BankGuaranteeDocumentMapper bankGuaranteeDocumentMapper;
    @Mock
    private ContractDocumentMapper contractDocumentMapper;
    @Mock
    private ConnectDotProperties connectDotProperties;

    @InjectMocks
    private ConnectDotMapper connectDotMapper;

    @ParameterizedTest
    @MethodSource("provideDocumentTypes")
    void shouldMapConnectDotRequestForBankGuarantee(DocumentType documentType){
        // BidBond guarantee
        BidBond bidBond = MockHelper.getBidBond();

        // GuaranteeDetailsData
        GuaranteeDetailsData<Object> guaranteeDetails = MockHelper.getGuaranteeDetailsData(BankGuaranteeCode.BID_BOND, bidBond);

        // BankGuaranteeRequestData
        BankGuaranteeRequestData requestData = getBankGuaranteeRequestData(guaranteeDetails, BankGuaranteeRecipient.BENEFICIARY, false);

        // ConnectDotInput
        ConnectDotInput connectDotInput = getConnectDotInput(requestData, documentType);

        ConnectDotRequest<?> result = connectDotMapper.prepareConnectDotRequest(connectDotInput);

        // Assert
        assertThat(result).isNotNull();

    }

    private static Stream<Arguments> provideDocumentTypes() {
        return Stream.of(
                Arguments.of(DocumentType.BG_DRAFT),
                Arguments.of(DocumentType.CONTRACT)
        );
    }
}
