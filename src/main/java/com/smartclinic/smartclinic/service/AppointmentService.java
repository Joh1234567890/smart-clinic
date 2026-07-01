package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.AppointmentActionResponse;
import com.smartclinic.smartclinic.dto.BookAppointmentRequest;
import com.smartclinic.smartclinic.entity.*;
import com.smartclinic.smartclinic.exception.ForbiddenOperationException;
import com.smartclinic.smartclinic.exception.InvalidStatusTransitionException;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.AppointmentRepository;
import com.smartclinic.smartclinic.repository.DoctorRepository;
import com.smartclinic.smartclinic.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Appointment business logic: booking, approval/rejection/completion,
 * and ownership-aware retrieval.
 *
 * Status lifecycle (enforced here, not in the controller):
 *   PENDING  -> APPROVED   (doctor/admin)
 *   PENDING  -> REJECTED   (doctor/admin)
 *   APPROVED -> COMPLETED  (doctor only)
 * Any other transition is rejected with InvalidStatusTransitionException.
 *
 * Ownership rules (also enforced here):
 *   - A PATIENT may only act on/view their own appointments.
 *   - A DOCTOR may only act on/view appointments assigned to them.
 *   - An ADMIN may act on/view all appointments.
 * Controllers only decide whether the *role* is allowed to call an
 * endpoint at all (e.g. @PreAuthorize("hasRole('DOCTOR')")); whether
 * this specific doctor is allowed to touch this specific appointment is
 * a business rule and lives here.
 *
 * Booking/approving/rejecting each also trigger a simulated email
 * notification via NotificationService - see bookAppointment,
 * approveAppointment, rejectAppointment.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;

    // ------------------------------------------------------------------
    // Booking
    // ------------------------------------------------------------------

    /**
     * A PATIENT books an appointment with a chosen doctor. The patient
     * is always the currently authenticated user - never taken from the
     * request body - and status always starts at PENDING.
     */
    public Appointment bookAppointment(BookAppointmentRequest request) {
        Patient patient = currentUserResolver.getCurrentPatient();

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.getDate());
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    /**
     * Same as bookAppointment, but also triggers the "booking
     * confirmation" simulated email and returns the message that
     * resulted, for the controller to pass through to the frontend.
     */
    public AppointmentActionResponse bookAppointmentWithNotification(BookAppointmentRequest request) {
        Appointment booked = bookAppointment(request);
        String message = notificationService.notifyAppointmentBooked(booked);
        return new AppointmentActionResponse(booked, message);
    }

    // ------------------------------------------------------------------
    // Retrieval
    // ------------------------------------------------------------------

    /**
     * GET /api/appointments/my - the current PATIENT's own appointments.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getMyAppointments() {
        Patient patient = currentUserResolver.getCurrentPatient();
        return appointmentRepository.findByPatientId(patient.getId());
    }

    /**
     * GET /api/appointments/all - ADMIN sees every appointment; DOCTOR
     * sees only appointments assigned to them. (Endpoint-level
     * @PreAuthorize already restricts this to ADMIN/DOCTOR; this method
     * decides which subset each of those two roles actually gets.)
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAllOrAssignedAppointments() {
        User currentUser = currentUserResolver.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return appointmentRepository.findAll();
        }

        // DOCTOR
        Doctor doctor = currentUserResolver.getCurrentDoctor();
        return appointmentRepository.findByDoctorId(doctor.getId());
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    // ------------------------------------------------------------------
    // Status transitions
    // ------------------------------------------------------------------

    /**
     * PUT /api/appointments/{id}/approve - DOCTOR or ADMIN.
     * A doctor may only approve appointments assigned to them; an admin
     * may approve any. Only valid starting from PENDING.
     */
    public Appointment approveAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        assertCallerMayManage(appointment);
        assertTransition(appointment.getStatus(), AppointmentStatus.APPROVED);
        appointment.setStatus(AppointmentStatus.APPROVED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Same as approveAppointment, but also triggers the "approval"
     * simulated email and returns the message that resulted.
     */
    public AppointmentActionResponse approveAppointmentWithNotification(Long id) {
        Appointment approved = approveAppointment(id);
        String message = notificationService.notifyAppointmentApproved(approved);
        return new AppointmentActionResponse(approved, message);
    }

    /**
     * PUT /api/appointments/{id}/reject - DOCTOR or ADMIN.
     * Only valid starting from PENDING.
     */
    public Appointment rejectAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        assertCallerMayManage(appointment);
        assertTransition(appointment.getStatus(), AppointmentStatus.REJECTED);
        appointment.setStatus(AppointmentStatus.REJECTED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Same as rejectAppointment, but also triggers the "cancellation"
     * simulated email and returns the message that resulted.
     */
    public AppointmentActionResponse rejectAppointmentWithNotification(Long id) {
        Appointment rejected = rejectAppointment(id);
        String message = notificationService.notifyAppointmentCancelled(rejected);
        return new AppointmentActionResponse(rejected, message);
    }

    /**
     * PUT /api/appointments/{id}/complete - DOCTOR only (an admin
     * approving/rejecting on a doctor's behalf is reasonable; marking a
     * consultation "completed" on a doctor's behalf is not, so this one
     * is intentionally not opened up to ADMIN).
     * Only valid starting from APPROVED.
     */
    public Appointment completeAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);

        Doctor caller = currentUserResolver.getCurrentDoctor();
        if (!appointment.getDoctor().getId().equals(caller.getId())) {
            throw new ForbiddenOperationException("You can only complete appointments assigned to you");
        }

        assertTransition(appointment.getStatus(), AppointmentStatus.COMPLETED);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Validates a requested transition against the strict lifecycle:
     *   PENDING  -> APPROVED | REJECTED
     *   APPROVED -> COMPLETED
     * Everything else (including transitions out of REJECTED or
     * COMPLETED, which are terminal) is rejected.
     */
    private void assertTransition(AppointmentStatus from, AppointmentStatus to) {
        boolean valid = switch (to) {
            case APPROVED, REJECTED -> from == AppointmentStatus.PENDING;
            case COMPLETED -> from == AppointmentStatus.APPROVED;
            case PENDING -> false; // nothing transitions back to PENDING
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(
                    "Cannot move appointment from " + from + " to " + to);
        }
    }

    /**
     * Shared ownership check for approve/reject: ADMIN may manage any
     * appointment; a DOCTOR may only manage appointments assigned to them.
     */
    private void assertCallerMayManage(Appointment appointment) {
        User currentUser = currentUserResolver.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        Doctor caller = currentUserResolver.getCurrentDoctor();
        if (!appointment.getDoctor().getId().equals(caller.getId())) {
            throw new ForbiddenOperationException("You can only manage appointments assigned to you");
        }
    }

    // ------------------------------------------------------------------
    // Plain CRUD retained from Phase 1 (generic admin-style management)
    // ------------------------------------------------------------------

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {
        Appointment existing = getAppointmentById(id);
        existing.setDate(updatedAppointment.getDate());
        existing.setStatus(updatedAppointment.getStatus());
        existing.setPatient(updatedAppointment.getPatient());
        existing.setDoctor(updatedAppointment.getDoctor());
        return appointmentRepository.save(existing);
    }

    public void deleteAppointment(Long id) {
        Appointment existing = getAppointmentById(id);
        appointmentRepository.delete(existing);
    }
}
