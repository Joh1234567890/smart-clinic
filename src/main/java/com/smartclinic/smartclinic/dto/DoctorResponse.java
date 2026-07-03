package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Doctor;
import lombok.Getter;
import lombok.Setter;

/**
 * Public-facing shape for a Doctor. Built explicitly from the entity so the
 * API never has to serialize the (possibly lazy-proxied) User association
 * directly, and never leaks User internals like role/id beyond name+email.
 */
@Getter
@Setter
public class DoctorResponse {

    private Long id;
    private String specialization;
    private String doctorName;
    private String email;

    public DoctorResponse(Long id, String specialization, String doctorName, String email) {
        this.id = id;
        this.specialization = specialization;
        this.doctorName = doctorName;
        this.email = email;
    }

    public static DoctorResponse from(Doctor doctor) {
        var user = doctor.getUser();
        return new DoctorResponse(
                doctor.getId(),
                doctor.getSpecialization(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null
        );
    }
}
