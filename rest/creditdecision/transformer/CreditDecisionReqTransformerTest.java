package com.ing.bankguarantees.remote.rest.creditdecision.transformer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.rest.creditdecision.CreditDecisionProperties;
import com.ing.bankguarantees.remote.rest.creditdecision.model.request.CreditDecisionInput;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreditDecisionReqTransformerTest {


    private static final String URL_FORMAT = "https://api.ing.com/sds-rt/credit-decision";
    private static final String APPLICATION_NAME = "BankGuaranteesBEOnline_API";
    private static final String REQUEST_TYPE = "6";
    private static final String DEFAULT_BRANCH = "390012";
    private static final String EUR_CURRENCY = "003";
    private static final Integer CSI_LENGTH = 10;
    private static final String PRIVATE_USE = "2";
    private static final String PRODUCT_TYPE = "15";
    private static final String PRODUCT_NATURE_TYPE = "0416";
    private static final String IND_PARTNERSHIP_TYPE = "20";
    private static final String REQUEST_CHANNEL = "01";
    private static final String ORG_PARTNERSHIP_TYPE = "10";
    private static final Integer WORK_STABILITY = 0;
    private static final Integer CAPITAL_PAYMENT_FREQUENCY = 1;
    private static final String ORG_INTERVENIENT_TYPE = "1";
    private static final String IND_INTERVENIENT_TYPE = "9";
    private static final Integer DURATION_IN_MONTH = 84;
    private static final String AMSTERDAM_ZONE_ID = "Europe/Brussels";
    @Mock
    private CreditDecisionProperties creditDecisionProperties;

    @Test
    void transformTest() {

        given(creditDecisionProperties.getRequestType()).willReturn(REQUEST_TYPE);
        given(creditDecisionProperties.getEurCurrency()).willReturn(EUR_CURRENCY);
        given(creditDecisionProperties.getCsiLength()).willReturn(CSI_LENGTH);
        given(creditDecisionProperties.getPrivateUse()).willReturn(PRIVATE_USE);
        given(creditDecisionProperties.getCapitalPaymentFrequency()).willReturn(CAPITAL_PAYMENT_FREQUENCY);
        given(creditDecisionProperties.getDefaultBranch()).willReturn(DEFAULT_BRANCH);
        given(creditDecisionProperties.getGreenPdlBusinessLineDurationInMonths()).willReturn(DURATION_IN_MONTH);
        given(creditDecisionProperties.getIndividualIntervenientType()).willReturn(IND_INTERVENIENT_TYPE);
        given(creditDecisionProperties.getIndividualPartnershipType()).willReturn(IND_PARTNERSHIP_TYPE);
        given(creditDecisionProperties.getLegalEntityWorkStability()).willReturn(WORK_STABILITY);
        given(creditDecisionProperties.getSelfEmployedWorkStability()).willReturn(WORK_STABILITY);
        given(creditDecisionProperties.getOrganisationIntervenientType()).willReturn(ORG_INTERVENIENT_TYPE);
        given(creditDecisionProperties.getProductType()).willReturn(PRODUCT_TYPE);
        given(creditDecisionProperties.getOrganisationPartnershipType()).willReturn(ORG_PARTNERSHIP_TYPE);
        given(creditDecisionProperties.getProductNatureType()).willReturn(PRODUCT_NATURE_TYPE);
        given(creditDecisionProperties.getRequestChannel()).willReturn(REQUEST_CHANNEL);
        CreditDecisionInput creditRiskScoreIn = MockHelper.getCreditRiskScoreInput();

        String timeZone = DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now(ZoneId.of(AMSTERDAM_ZONE_ID)));
        String expected = getExpectedString(creditRiskScoreIn, timeZone);

        Request request = new CreditDecisionReqTransformer(URL_FORMAT, APPLICATION_NAME, creditDecisionProperties).transform(creditRiskScoreIn);
        assertThat(request.uri()).isEqualTo("/sds-rt/credit-decision");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.getContentString()).isEqualTo(expected);
    }

    @Test
    void transformErrorTest() {

        given(creditDecisionProperties.getRequestChannel()).willThrow(new BgosException(ErrorCode.INVALID_REQUEST));
        CreditDecisionReqTransformer creditRiskReqTransformer = new CreditDecisionReqTransformer(URL_FORMAT, APPLICATION_NAME, creditDecisionProperties);
        CreditDecisionInput creditRiskScoreIn = MockHelper.getCreditRiskScoreInput();
        BgosException bgosException = assertThrows(BgosException.class, () -> creditRiskReqTransformer.transform(creditRiskScoreIn));
        assertNotNull(bgosException);
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private static @NotNull String getExpectedString(CreditDecisionInput creditRiskScoreIn, String timeZone) {
        String expected = "{\"requestId\":%s,\"requestDate\":\"%s\",\"productType\":\"15\",\"requestChannel\":\"01\"," +
                "\"requestType\":\"6\",\"openingBranch\":\"390012\",\"followupBranch\":\"390012\",\"professionalUseFlag\":true," +
                "\"agreementCurrency\":\"003\",\"creditOperations\":[{\"productNatureType\":\"0416\",\"creditAmount\":3124545.34," +
                "\"durationMonths\":84,\"capitalPaymentFrequency\":1,\"privateUse\":\"2\",\"operationProductType\":\"15\"}]," +
                "\"involvedparties\":[{\"involvedPartyInternalIdentifier\":{\"id\":\"999999999999\",\"type\":\"CSI_BE\"}," +
                "\"workStability\":0,\"partnershipType\":\"20\",\"intervenientType\":\"9\"}," +
                "{\"involvedPartyInternalIdentifier\":{\"id\":\"0123456789\",\"type\":\"CSI_BE\"},\"workStability\":0,\"partnershipType\":\"10\"," +
                "\"intervenientType\":\"1\"}]}";

        expected = String.format(expected, creditRiskScoreIn.requestId(), timeZone);
        return expected;
    }

}
