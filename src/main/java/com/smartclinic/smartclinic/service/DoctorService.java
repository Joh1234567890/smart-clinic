package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.DoctorResponse;
import com.smartclinic.smartclinic.entity.Doctor;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        return DoctorResponse.from(findDoctorEntity(id));
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        Doctor existing = findDoctorEntity(id);
        existing.setSpecialization(updatedDoctor.getSpecialization());
        existing.setUser(updatedDoctor.getUser());
        return doctorRepository.save(existing);
    }

    public void deleteDoctor(Long id) {
        Doctor existing = findDoctorEntity(id);
        doctorRepository.delete(existing);
    }

    private Doctor findDoctorEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }
}