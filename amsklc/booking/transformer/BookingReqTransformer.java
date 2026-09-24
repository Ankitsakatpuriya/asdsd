package com.ing.bankguarantees.remote.rest.amsklc.booking.transformer;

import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder;
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;
import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.common.RequestTransformer;
import com.ing.bankguarantees.remote.rest.amsklc.booking.BookingApiProperties;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingInput;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.BookingRequest;
import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import com.ing.bankguarantees.utils.JsonUtils;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationHelper.formatDate;
import static com.ing.bankguarantees.utils.ConstantUtils.DATE_TIME_FORMATTER_YYYY_MM_DD;
import static com.ing.bankguarantees.utils.ConstantUtils.HEADER_RCID;

@Slf4j
@Component
public class BookingReqTransformer extends RequestTransformer<BookingRequest> {

    private static final String ING_SOURCE = "X-ING-SOURCE";
    private static final String BG_BE = "BG-BE";
    private final String applicationName;
    private final BookingApiProperties bookingApiProperties;

    public BookingReqTransformer(@Value("${rest.api-booking-loan-api-url}") String urlFormat,
                                 @Value("${bgos.service-name}") String applicationName,
                                 BookingApiProperties bookingApiProperties) {
        super(urlFormat);
        this.applicationName = applicationName;
        this.bookingApiProperties = bookingApiProperties;

    }

    @Override
    public Request transform(BookingRequest bookingRequest) {
        try {

            BookingInput bookingInput = prepareBookingInput(bookingRequest);

            Request request = new RichHttpRequestBuilder()
                    .withMethod(Method.Post())
                    .withUrl(getUrlFormat())
                    .withJsonContent(bookingInput)
                    .withHeader(HEADER_RCID, BG_BE)
                    .withHeader(ING_SOURCE, applicationName)
                    .build();
            log.info(C3LogMarker.marker, "Calling AMS BE KLC Booking API endpoint [{}] with requestPayload {} and method {}",
                    request.hashCode(), JsonUtils.getJsonFromObject(bookingInput), bookingRequest.requestType());
            return request;
        } catch (RichHttpRequestBuilderException ex) {
            log.error("Error while parsing the request to call Party Search api {}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    private BookingInput prepareBookingInput(BookingRequest bookingRequest) {
        return BookingInput.builder()
                .dataSource(bookingApiProperties.getDataSource())
                .creationLinkCode(bookingApiProperties.getCreationLinkCode())
                .destinationOfFunds(bookingRequest.accountNumber())
                .mutationCode(bookingRequest.requestType() == RequestType.CREATE ? bookingApiProperties.getCreateMutationCode()
                        : bookingApiProperties.getMutationCode())
                .statusLotAVOI(bookingRequest.requestType() == RequestType.CREATE ? bookingApiProperties.getCreateStatusLotAvoi()
                        : bookingApiProperties.getStatusLotAvoi())
                .rangeNumber(bookingRequest.requestType() == RequestType.CREATE ? bookingApiProperties.getRangeNumber() : null)
                .productCode(bookingApiProperties.getProductCode())
                .systemBookDate(bookingRequest.requestType() == RequestType.CREATE ? getCurrentDate() : null)
                .agreementIdentifier(bookingRequest.requestType() == RequestType.CREATE ? prepareAgreementIdentifier(bookingRequest)
                        : null)
                .initialAmount(prepareInitialAmount(bookingRequest))
                .eventIdentifier(prepareEventIdentifier(bookingRequest)).build();
    }

    private Integer getCurrentDate() {
        return Integer.valueOf(String.join("", Arrays.asList(formatDate(LocalDate.now(), DATE_TIME_FORMATTER_YYYY_MM_DD).split("-"))));
    }

    private BookingInput.EventIdentifier prepareEventIdentifier(BookingRequest bookingRequest) {
        return BookingInput.EventIdentifier.builder()
                .type(bookingApiProperties.getEventIdentifierType())
                .value(bookingRequest.reservationNumber())
                .build();
    }

    private BookingInput.InitialAmount prepareInitialAmount(BookingRequest bookingRequest) {
        return BookingInput.InitialAmount.builder()
                .currency(BookingInput.InitialAmount.Currency.builder()
                        .code(bookingRequest.currency())
                        .build())
                .value(bookingRequest.bgAmount())
                .build();
    }

    private BookingInput.AgreementIdentifier prepareAgreementIdentifier(BookingRequest bookingRequest) {
        return BookingInput.AgreementIdentifier.builder()
                .type(bookingApiProperties.getAgreementIdentifierType())
                .value(bookingRequest.klcNumber())
                .build();
    }
}

