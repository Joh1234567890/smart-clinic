package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Doctor;
import com.smartclinic.smartclinic.entity.MedicalRecord;
import com.smartclinic.smartclinic.entity.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Public-facing shape for a MedicalRecord. Built explicitly from the
 * entity so the API never has to serialize the (lazy-proxied)
 * Doctor/Patient associations directly.
 */
@Getter
@Setter
public class MedicalRecordResponse {

    private Long id;
    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDateTime createdAt;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;

    public MedicalRecordResponse(Long id, String diagnosis, String treatment, String notes,
                                  LocalDateTime createdAt, Long appointmentId,
                                  Long doctorId, String doctorName,
                                  Long patientId, String patientName) {
        this.id = id;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.createdAt = createdAt;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
    }

    public static MedicalRecordResponse from(MedicalRecord record) {
        Doctor doctor = record.getDoctor();
        Patient patient = record.getPatient();

        return new MedicalRecordResponse(
                record.getId(),
                record.getDiagnosis(),
                record.getTreatment(),
                record.getNotes(),
                record.getCreatedAt(),
                record.getAppointmentId(),
                doctor != null ? doctor.getId() : null,
                doctor != null && doctor.getUser() != null ? doctor.getUser().getName() : null,
                patient != null ? patient.getId() : null,
                patient != null && patient.getUser() != null ? patient.getUser().getName() : null
        );
    }
}
