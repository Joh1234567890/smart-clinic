package com.smartclinic.smartclinic.security;

import com.smartclinic.smartclinic.entity.Doctor;
import com.smartclinic.smartclinic.entity.Patient;
import com.smartclinic.smartclinic.entity.User;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.DoctorRepository;
import com.smartclinic.smartclinic.repository.PatientRepository;
import com.smartclinic.smartclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves "who is making this request" from the Spring Security context
 * that JwtAuthFilter populated, and bridges that down to the caller's
 * Patient or Doctor profile id.
 *
 * This exists because the JWT only carries email + role - it has no
 * concept of "patient id" or "doctor id". Ownership checks like "a
 * patient can only view their own appointments" need to go: JWT subject
 * (email) -> User -> Patient/Doctor row -> compare ids. Centralizing
 * that lookup here keeps AppointmentService and MedicalRecordService
 * focused on business rules rather than repeating this plumbing.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * The User backing the current request's JWT.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found for the authenticated email: " + email));
    }

    /**
     * The Patient profile linked to the current user.
     * Throws if the current user has no Patient profile (e.g. they are
     * a DOCTOR or ADMIN) - callers should only invoke this after
     * confirming the caller's role is PATIENT.
     */
    public Patient getCurrentPatient() {
        User user = getCurrentUser();
        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile for current user", user.getId()));
    }

    /**
     * The Doctor profile linked to the current user.
     * Throws if the current user has no Doctor profile - callers should
     * only invoke this after confirming the caller's role is DOCTOR.
     */
    public Doctor getCurrentDoctor() {
        User user = getCurrentUser();
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile for current user", user.getId()));
    }
}
