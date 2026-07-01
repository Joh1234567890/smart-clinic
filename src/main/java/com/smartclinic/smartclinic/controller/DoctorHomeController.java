package com.smartclinic.smartclinic.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstration endpoint for the "/doctor/**" access rule defined in
 * SecurityConfig (hasRole("DOCTOR")). Only a JWT carrying the DOCTOR
 * role can reach this.
 *
 * Named DoctorHomeController (not DoctorController) to avoid colliding
 * with the existing DoctorController, which handles CRUD on the Doctor
 * entity at "/api/doctors" and isn't role-restricted by path prefix.
 * Real doctor-only endpoints (e.g. a doctor's own appointment schedule,
 * writing medical records for their patients) would live under this
 * same "/doctor/**" prefix going forward.
 */
@RestController
@RequestMapping("/doctor")
public class DoctorHomeController {

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return Map.of(
                "message", "Welcome, doctor.",
                "authenticatedAs", authentication.getName()
        );
    }
}
