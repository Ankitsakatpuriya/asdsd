package com.ing.bankguarantees.remote.rest.amsklc.booking;

import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingResponse;
import com.ing.bankguarantees.remote.rest.amsklc.booking.transformer.BookingResTransformer;
import com.ing.bankguarantees.util.MockHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookingResTransformerTest {

    @Test
    void transformTest() {
        BookingResponse bookingResponse = MockHelper.getBookingResponse();
        var bookingResTransformer = new BookingResTransformer();
        var response = bookingResTransformer.transform(bookingResponse);
        assertThat(response).isNotNull().isEqualTo("value");
    }
}
