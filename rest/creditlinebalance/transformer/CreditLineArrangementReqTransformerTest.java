package com.ing.bankguarantees.remote.rest.creditlinebalance.transformer;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.CreditLineArrangementProperties;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.mapper.CreditLineArrangementRequestMapper;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.transformer.CreditLineArrangementReqTransformer;
import com.ing.bankguarantees.remote.utils.RequestAdapter;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreditLineArrangementReqTransformerTest {


    private static final String URL_FORMAT = "https://api.ing.com/api/klc/creditlinearrangement/update";
    private static final String APPLICATION_NAME = "BankGuaranteesBEOnline_API";
    private static final String FILE_NAME = "BGA/CLD/credit_Line_arr.json";

    @Mock
    private CreditLineArrangementProperties creditLineArrangementProperties;
    @Mock
    private CreditLineArrangementRequestMapper creditLineArrangementRequestMapper;

    @Test
    void transformTest() {

        var creditLineArrangementInput = JsonUtils.mapFileToObject(FILE_NAME, CreditLineArrangementInput.class);
        when(creditLineArrangementRequestMapper.prepareCreditLineArrangementInput(any())).thenReturn(creditLineArrangementInput);
        Request request = new CreditLineArrangementReqTransformer(URL_FORMAT, APPLICATION_NAME, creditLineArrangementProperties,
                creditLineArrangementRequestMapper).transform(RequestAdapter.getCreditLineArrangementRequest("BG00012",
                RequestType.CREATE, "123456789", 123, "003"));
        assertThat(request.uri()).isEqualTo("/api/klc/creditlinearrangement/update");
        assertThat(request.method()).isEqualTo(Method.Post());
        assertThat(request.getContentString()).isEqualTo(JsonUtils.getJsonFromObject(creditLineArrangementInput));
    }

    @Test
    void transformErrorTest() {
        given(creditLineArrangementRequestMapper.prepareCreditLineArrangementInput(any())).willThrow(new BgosException(ErrorCode.INVALID_REQUEST));

        var CreditLineArrangementReqTransformer = new CreditLineArrangementReqTransformer(URL_FORMAT, APPLICATION_NAME, creditLineArrangementProperties,
                creditLineArrangementRequestMapper);
        BgosException bgosException = assertThrows(BgosException.class, () -> CreditLineArrangementReqTransformer.transform(RequestAdapter.getCreditLineArrangementRequest("BG00012",
                RequestType.CREATE, "123456789", 123, "003")));
        Assertions.assertNotNull(bgosException);
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }

}

