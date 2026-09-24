package com.ing.bankguarantees.remote.rest.holidaycalendar.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NotNull
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankHoliday {
    private LocalDate date;
}
