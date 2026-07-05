package com.smartclinic.smartclinic.controller;

import com.smartclinic.smartclinic.dto.AppointmentActionResponse;
import com.smartclinic.smartclinic.dto.AppointmentResponse;
import com.smartclinic.smartclinic.dto.BookAppointmentRequest;
import com.smartclinic.smartclinic.entity.Appointment;
import com.smartclinic.smartclinic.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for Appointment, both the Phase 3 business workflow
 * (book/my/all/approve/reject/complete) and the Phase 1 generic CRUD
 * retained for direct admin-style management.
 *
 * Base path: /api/appointments. Per the Phase 3 spec's suggested paths
 * (/appointments/book, /appointments/my, etc.) but nested under the
 * already-existing /api/appointments prefix rather than introducing a
 * second, parallel URL namespace for the same resource.
 *
 * Role checks here only gate *which role* may call the endpoint at all.
 * Ownership checks (e.g. "is this actually your appointment") happen in
 * AppointmentService, since that's a business rule, not a URL-pattern
 * rule, and the spec requires status validation to live in the service
 * layer.
 *
 * book/approve/reject return AppointmentActionResponse (the appointment
 * plus a simulated-email confirmation message - Phase 5) rather than a
 * bare Appointment, so the frontend can display e.g. "Email sent
 * successfully to patient." using a message the backend actually
 * generated.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ------------------------------------------------------------------
    // Phase 3: booking & workflow
    // ------------------------------------------------------------------

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/book")
    public ResponseEntity<AppointmentActionResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        AppointmentActionResponse result = appointmentService.bookAppointmentWithNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointmentsForCaller() {
        return ResponseEntity.ok(appointmentService.getAllOrAssignedAppointments());
    }

    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<AppointmentActionResponse> approveAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.approveAppointmentWithNotification(id));
    }

    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<AppointmentActionResponse> rejectAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.rejectAppointmentWithNotification(id));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(AppointmentResponse.from(appointmentService.completeAppointment(id)));
    }

    // ------------------------------------------------------------------
    // Phase 1: generic CRUD, retained for ADMIN-style direct management
    // ------------------------------------------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody Appointment appointment) {
        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment appointment) {
        return ResponseEntity.ok(AppointmentResponse.from(appointmentService.updateAppointment(id, appointment)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
