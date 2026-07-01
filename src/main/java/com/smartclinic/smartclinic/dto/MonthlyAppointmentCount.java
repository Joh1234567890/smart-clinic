package com.smartclinic.smartclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * One point on the "appointments per month" chart.
 * monthLabel is pre-formatted (e.g. "Jan 2026") so the frontend doesn't
 * need its own year/month-number-to-name mapping logic.
 */
@Getter
@Setter
@AllArgsConstructor
public class MonthlyAppointmentCount {
    private String monthLabel;
    private long count;
}
