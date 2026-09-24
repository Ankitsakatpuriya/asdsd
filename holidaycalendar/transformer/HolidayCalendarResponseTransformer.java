package com.ing.bankguarantees.remote.rest.holidaycalendar.transformer;

import com.ing.bankguarantees.remote.common.Transformer;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.BankHoliday;
import com.ing.bankguarantees.remote.rest.holidaycalendar.model.HolidayCalendarResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HolidayCalendarResponseTransformer implements Transformer<HolidayCalendarResponse, List<BankHoliday>> {

    @Override
    public List<BankHoliday> transform(HolidayCalendarResponse response) {

        return response.getHolidays().stream()
                .map(holiday -> BankHoliday.builder()
                        .date(holiday.getDate())
                        .build())
                .collect(Collectors.toList());

    }
}
