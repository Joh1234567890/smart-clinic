package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.AuthResponse;
import com.smartclinic.smartclinic.dto.LoginRequest;
import com.smartclinic.smartclinic.dto.RegisterRequest;
import com.smartclinic.smartclinic.entity.*;
import com.smartclinic.smartclinic.repository.DoctorRepository;
import com.smartclinic.smartclinic.repository.PatientRepository;
import com.smartclinic.smartclinic.repository.UserRepository;
import com.smartclinic.smartclinic.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ============================
    // REGISTER
    // ============================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (request.getRole() == Role.PATIENT) {
            if (request.getAge() == null || request.getGender() == null) {
                throw new IllegalArgumentException("Age and gender required for PATIENT");
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        // Explicit profile creation (NO entity navigation)
        if (saved.getRole() == Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(saved);
            patient.setAge(request.getAge());
            patient.setGender(request.getGender());
            patientRepository.save(patient);
        }

        if (saved.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(saved);
            doctor.setSpecialization("General");
            doctorRepository.save(doctor);
        }

        String token = jwtService.generateToken(saved, saved.getRole().name());

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole()
        );
    }

    // ============================
    // LOGIN
    // ============================
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // SAFE: single clean query (no lazy loading triggers)
        User user = userRepository.findByEmailOnly(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user, user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}