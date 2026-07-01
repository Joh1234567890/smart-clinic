package com.smartclinic.smartclinic.entity;

/**
 * Roles a User can hold in the system.
 * ADMIN is included for completeness even though no admin-specific
 * logic exists yet (no auth/security has been wired up).
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN
}
