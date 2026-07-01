package com.smartclinic.smartclinic.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstration endpoint for the "/patient/**" access rule defined in
 * SecurityConfig (hasRole("PATIENT")). Only a JWT carrying the PATIENT
 * role can reach this.
 *
 * Named PatientHomeController (not PatientController) to avoid colliding
 * with the existing PatientController, which handles CRUD on the Patient
 * entity at "/api/patients" and isn't role-restricted by path prefix.
 * Real patient-only endpoints (e.g. a patient's own appointment history)
 * would live under this same "/patient/**" prefix going forward.
 */
@RestController
@RequestMapping("/patient")
public class PatientHomeController {

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return Map.of(
                "message", "Welcome, patient.",
                "authenticatedAs", authentication.getName()
        );
    }
}
