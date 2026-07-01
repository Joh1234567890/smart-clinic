package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Returned from booking/approve/reject so the frontend can show both the
 * updated appointment and the exact simulated-email confirmation message
 * the backend generated - e.g. "Email sent successfully to patient." -
 * rather than the frontend hardcoding that same string independently of
 * whatever the backend actually did.
 */
@Getter
@Setter
@AllArgsConstructor
public class AppointmentActionResponse {
    private Appointment appointment;
    private String notificationMessage;
}
