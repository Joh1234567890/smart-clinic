package com.smartclinic.smartclinic.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstration endpoint for the "/admin/**" access rule defined in
 * SecurityConfig (hasRole("ADMIN")). Any request here without a valid
 * JWT belonging to an ADMIN user is rejected before this method ever runs.
 *
 * Real admin-only business endpoints (e.g. managing all appointments
 * system-wide, viewing audit logs) would live under this same
 * "/admin/**" prefix going forward.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return Map.of(
                "message", "Welcome, admin.",
                "authenticatedAs", authentication.getName()
        );
    }
}
