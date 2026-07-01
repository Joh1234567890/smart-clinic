package com.smartclinic.smartclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /api/medical-records/create.
 *
 * The caller (a DOCTOR) only specifies which appointment this record is
 * for, plus the clinical content. patient and doctor are both derived
 * server-side from the appointment (and cross-checked against the
 * caller's own Doctor profile) rather than trusted from the request -
 * a doctor should not be able to write a record under another doctor's
 * name, or attach it to a patient that doesn't match the appointment.
 */
@Getter
@Setter
public class CreateMedicalRecordRequest {

    @NotNull(message = "appointmentId is required")
    private Long appointmentId;

    @NotBlank(message = "diagnosis is required")
    private String diagnosis;

    @NotBlank(message = "treatment is required")
    private String treatment;

    private String notes;
}
