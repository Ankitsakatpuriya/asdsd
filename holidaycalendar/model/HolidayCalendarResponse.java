package com.ing.bankguarantees.remote.rest.holidaycalendar.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class HolidayCalendarResponse {

    private String center;

    @NotEmpty
    private List<@Valid HolidayResponse> holidays;

}

