package com.ing.bankguarantees.remote.rest.kfccreditoverview.transformer;

import com.ing.bankguarantees.remote.rest.kfccreditoverview.KfcCreditOverviewProperties;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CustomerArrangementReqTransformerTest {

    private static final String URL_FORMAT = "https://api.ing.com/api/arrangement-reporting/customer-arrangement-details";
    private static final String APPLICATION_NAME = "BankGuaranteesBEOnline_API";
    private static final String IBAN_NUMBER = "BE78335024399586";

    @Test
    void transformTest() {
        KfcCreditOverviewProperties kfcCreditOverviewProperties = MockHelper.getKfcCreditOverviewProperties();
        Request request = new CustomerArrangementReqTransformer(URL_FORMAT, APPLICATION_NAME, kfcCreditOverviewProperties).transform(IBAN_NUMBER);
        assertThat(request.uri()).isEqualTo("/api/arrangement-reporting/customer-arrangement-details");
        assertThat(request.getContentString()).isEqualTo("{\"involvedPartyIdentifiers\":[{\"type\":\"ACC\",\"value\":\"BE78335024399586\"}],\"employeeId\":{\"type\":\"MATRICULE\",\"value\":\"841020413\"},\"level\":21}");
    }

}
