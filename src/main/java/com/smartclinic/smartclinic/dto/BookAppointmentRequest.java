package com.smartclinic.smartclinic.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Request body for POST /api/appointments/book.
 *
 * The caller (a PATIENT) only specifies which doctor and when - their
 * own Patient profile is resolved server-side from the JWT, and status
 * always starts at PENDING. This keeps a patient from being able to
 * book on someone else's behalf or set an arbitrary initial status.
 */
@Getter
@Setter
public class BookAppointmentRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotNull(message = "date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime date;
}
