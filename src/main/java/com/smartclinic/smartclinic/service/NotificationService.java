package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Domain-specific notification messages for the appointment workflow.
 * Each method composes a message using real appointment/user data, asks
 * EmailService to "deliver" it (console-log simulation), and returns the
 * exact confirmation string the frontend should display - so the UI's
 * success message always reflects something the backend actually did,
 * rather than the frontend independently guessing the same text.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    private final EmailService emailService;

    /**
     * Sent to the patient immediately after they book a new appointment.
     */
    public String notifyAppointmentBooked(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getName();
        String when = appointment.getDate().format(DATE_FORMAT);

        String body = "Your appointment with Dr. " + doctorName + " on " + when
                + " has been booked and is pending approval.";
        emailService.sendEmail(patientEmail, "Appointment Booked", body);

        return "Email sent successfully to patient.";
    }

    /**
     * Sent to the patient when a doctor/admin approves their PENDING
     * appointment.
     */
    public String notifyAppointmentApproved(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getName();
        String when = appointment.getDate().format(DATE_FORMAT);

        String body = "Your appointment with Dr. " + doctorName + " on " + when + " has been approved.";
        emailService.sendEmail(patientEmail, "Appointment Approved", body);

        return "Appointment approval email sent.";
    }

    /**
     * Sent to the patient when a doctor/admin rejects (cancels) their
     * PENDING appointment. The spec's UI-facing language for this action
     * is "cancellation" even though the underlying status is REJECTED -
     * see AppointmentStatus's javadoc for why there's no separate
     * CANCELLED status in this system.
     */
    public String notifyAppointmentCancelled(Appointment appointment) {
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String doctorName = appointment.getDoctor().getUser().getName();
        String when = appointment.getDate().format(DATE_FORMAT);

        String body = "Your appointment with Dr. " + doctorName + " on " + when + " has been cancelled.";
        emailService.sendEmail(patientEmail, "Appointment Cancelled", body);

        return "Appointment cancellation email sent.";
    }
}
