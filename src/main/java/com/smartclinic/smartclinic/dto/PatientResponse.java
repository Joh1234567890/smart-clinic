package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Patient;
import lombok.Getter;
import lombok.Setter;

/**
 * Public-facing shape for a Patient. Built explicitly from the entity so
 * the API never has to serialize the (lazy-proxied) User association
 * directly.
 */
@Getter
@Setter
public class PatientResponse {

    private Long id;
    private Integer age;
    private String gender;
    private String name;
    private String email;

    public PatientResponse(Long id, Integer age, String gender, String name, String email) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.name = name;
        this.email = email;
    }

    public static PatientResponse from(Patient patient) {
        var user = patient.getUser();
        return new PatientResponse(
                patient.getId(),
                patient.getAge(),
                patient.getGender(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null
        );
    }
}
