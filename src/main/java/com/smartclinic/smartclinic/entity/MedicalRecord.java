package com.smartclinic.smartclinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Medical record produced by a doctor after an appointment.
 *
 * One-to-one with Appointment (appointment_id FK, owning side) - each
 * appointment can produce at most one record. patient and doctor are
 * also stored directly (not just derived by navigating through
 * appointment) so records can be queried straightforwardly by patient
 * or doctor id without an extra join, per the Phase 3 spec.
 */
@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @NotBlank
    @Column(name = "treatment", nullable = false)
    private String treatment;

    @Lob
    @Column(name = "notes")
    private String notes;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------

    // One-to-one owning side: medical_records.appointment_id -> appointments.id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    @JsonIgnore
    private Appointment appointment;

    // Many-to-one: medical_records.patient_id -> patients.id
    // Kept alongside `appointment` (rather than only derived from it) so
    // records can be queried directly by patient id without a join.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Many-to-one: medical_records.doctor_id -> doctors.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Exposes just the appointment's id in JSON responses without
    // serializing the whole nested Appointment object (which is
    // @JsonIgnore'd above to avoid recursing back through
    // Appointment.medicalRecord).
    @Transient
    public Long getAppointmentId() {
        return appointment != null ? appointment.getId() : null;
    }
}
