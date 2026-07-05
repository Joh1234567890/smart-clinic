package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Appointment;
import com.smartclinic.smartclinic.entity.AppointmentStatus;
import com.smartclinic.smartclinic.entity.Doctor;
import com.smartclinic.smartclinic.entity.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Public-facing shape for an Appointment. Built explicitly from the entity
 * so the API never has to serialize the (lazy-proxied) Doctor/Patient
 * associations directly.
 */
@Getter
@Setter
public class AppointmentResponse {

    private Long id;
    private LocalDateTime date;
    private AppointmentStatus status;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;

    public AppointmentResponse(Long id, LocalDateTime date, AppointmentStatus status,
                                Long doctorId, String doctorName, String doctorSpecialization,
                                Long patientId, String patientName) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.patientId = patientId;
        this.patientName = patientName;
    }

    public static AppointmentResponse from(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDate(),
                appointment.getStatus(),
                doctor != null ? doctor.getId() : null,
                doctor != null && doctor.getUser() != null ? doctor.getUser().getName() : null,
                doctor != null ? doctor.getSpecialization() : null,
                patient != null ? patient.getId() : null,
                patient != null && patient.getUser() != null ? patient.getUser().getName() : null
        );
    }
}
