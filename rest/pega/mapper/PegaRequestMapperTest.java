package com.ing.bankguarantees.remote.rest.pega.mapper;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.Document;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.domain.StpResultDataSet;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.enums.PegaCaseType;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.remote.rest.pega.PegaProperties;
import com.ing.bankguarantees.remote.rest.pega.model.Content;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseInput;
import com.ing.bankguarantees.remote.rest.pega.model.PegaCreateCaseRequest;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Stream;

import static com.ing.bankguarantees.util.MockHelper.getGuaranteeDetails;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PegaRequestMapperTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_STP_RESULT_FILE = "BGA/BGR/bg_stp_result.json";
    private static final String PEGA_PERFORMANCE_BOND_TYPE = "Performance Bond Guarantee";
    private static final String PEGA_RENTAL_TYPE = "Rental Guarantee";
    private static final String PEGA_ADVANCE_PAYMENT_TYPE = "Advance Payment Guarantee";
    private static final String PEGA_PAYMENT_GUARANTEE_TYPE = "Payment Guarantee";
    private static final String PEGA_RETENTION_TYPE = "Retention Guarantee";
    private static final String PEGA_BID_BOND_TYPE = "Bid Bond Guarantee";
    private static final String PEGA_CUSTOM_TYPE = "Customs Guarantee";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";
    private static final String DOCUMENT_ID = "344135tplow";

    @Mock
    private PegaProperties pegaProperties;

    @InjectMocks
    private PegaRequestMapper pegaRequestMapper;

    @ParameterizedTest
    @MethodSource("bgCodes")
    void preparePegaRequest(BankGuaranteeCode bankGuaranteeCode) {
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        StpResultDataSet stpResultDataset = MockHelper.createStpResultDataset(BG_STP_RESULT_FILE);
        bankGuaranteeRequestData.setStpResultDataSet(stpResultDataset);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetails = getGuaranteeDetails(bankGuaranteeCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetails);
        Document document = MockHelper.getDocument(UUID_ORG, DOCUMENT_ID, REQUESTER_ID);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        PegaCreateCaseInput pegaCaseInput = MockHelper.getPegaCaseInput(document, bankGuaranteeRequest, PegaCaseType.AMEND);
        PegaCreateCaseRequest pegaCreateCaseRequest = pegaRequestMapper.preparePegaRequest(pegaCaseInput);
        Content content = pegaCreateCaseRequest.content();
        String guaranteeType = getGuaranteeType(bankGuaranteeCode);
        assertThat(content.guaranteeType()).isEqualTo(guaranteeType);

    }

    private static Stream<BankGuaranteeCode> bgCodes() {
        return Stream.of(BankGuaranteeCode.values());
    }

    private String getGuaranteeType(BankGuaranteeCode bgCode) {
        return switch (bgCode) {
            case PERFORMANCE_BOND, PUBLIC_CONTRACT, STATE_LOTTERY, OVAM, DCK_CDC,
                 PASSENGER_TRANSPORT, GOODS_TRANSPORT, OPERATORS_TRANSPORT, CUSTOMIZED_TEXT ->
                    PEGA_PERFORMANCE_BOND_TYPE;
            case CUSTOM_1, CUSTOM_2, CUSTOM_4, CUSTOM_5 -> PEGA_CUSTOM_TYPE;
            case PAYMENT_GUARANTEE, REAL_ESTATE -> PEGA_PAYMENT_GUARANTEE_TYPE;
            case ADVANCE_PAYMENT -> PEGA_ADVANCE_PAYMENT_TYPE;
            case MONEY_RETENTION_BOND -> PEGA_RETENTION_TYPE;
            case BID_BOND -> PEGA_BID_BOND_TYPE;
            case RENTAL, WOODS_PROM_A, WOODS_PROM_B, WOODS_BGWAL_PUBLIC, WOODS_BG_PRIVATE, WOODS_BG_DISCHARGE,
                 WOODS_BGVLA_PUBLIC, WOODS_PROM_VLA,
                 ABSTRACT_PROM, PUBLIC_CONTRACT_PROM -> PEGA_RENTAL_TYPE;
        };

    }

}
