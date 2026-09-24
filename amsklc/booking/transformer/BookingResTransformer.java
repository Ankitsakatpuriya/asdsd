package com.ing.bankguarantees.remote.rest.amsklc.booking.transformer;


import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingResTransformer implements Transformer<BookingResponse, String> {

    @Override
    public String transform(BookingResponse bookingLoanResponse) {
        log.info("BookingResTransformer [transform ] Receive response for Booking API ");
        return bookingLoanResponse.agreementIdentifier().value();

    }
}
