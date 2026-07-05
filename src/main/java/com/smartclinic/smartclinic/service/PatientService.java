package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.PatientResponse;
import com.smartclinic.smartclinic.entity.Patient;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        return PatientResponse.from(findPatientEntity(id));
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient updatedPatient) {
        Patient existing = findPatientEntity(id);
        existing.setAge(updatedPatient.getAge());
        existing.setGender(updatedPatient.getGender());
        existing.setUser(updatedPatient.getUser());
        return patientRepository.save(existing);
    }

    public void deletePatient(Long id) {
        Patient existing = findPatientEntity(id);
        patientRepository.delete(existing);
    }

    private Patient findPatientEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }
}