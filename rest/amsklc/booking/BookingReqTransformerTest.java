package com.ing.bankguarantees.remote.rest.amsklc.booking;

import com.ing.bankguarantees.remote.rest.amsklc.booking.transformer.BookingReqTransformer;
import com.ing.bankguarantees.util.MockHelper;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookingReqTransformerTest {
    private static final String URL_FORMAT = "https://klc.api.ing.com/api/bookings";

    @Mock
    private BookingApiProperties bookingApiProperties;

    @Test
    void transformTest() {
        var bookingRequest = MockHelper.getBookingRequest();
        Request request = new BookingReqTransformer(URL_FORMAT, "applicationName", bookingApiProperties)
                .transform(bookingRequest);
        assertThat(request.uri()).isEqualTo("/api/bookings");
        assertThat(request.method()).isEqualTo(Method.Post());
    }
}