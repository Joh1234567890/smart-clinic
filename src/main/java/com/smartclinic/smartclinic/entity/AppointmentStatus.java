package com.smartclinic.smartclinic.entity;

/**
 * Lifecycle status of an Appointment.
 *
 * Enforced transition rules (validated in AppointmentService, not here
 * and not in the controller):
 *   PENDING   -> APPROVED   (doctor/admin approves a booked request)
 *   PENDING   -> REJECTED   (doctor/admin rejects a booked request)
 *   APPROVED  -> COMPLETED  (doctor marks the consultation done)
 *
 * No other transitions are allowed - e.g. a COMPLETED or REJECTED
 * appointment is terminal, and an appointment cannot be completed
 * without first being approved.
 */
public enum AppointmentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
}
