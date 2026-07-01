package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for POST /auth/register.
 *
 * Kept separate from the User entity so the API surface (what a client
 * is allowed to send) doesn't leak internal entity structure - e.g.
 * clients cannot set an id or directly attach a Patient/Doctor profile
 * through this endpoint.
 *
 * age/gender are only used (and required) when role == PATIENT, in
 * which case AuthService also creates the matching Patient profile row
 * automatically - otherwise a newly registered patient would have a
 * User but no Patient record, and booking an appointment would fail.
 * That check happens in AuthService rather than via @NotNull here,
 * since the requirement is conditional on another field's value.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    // Required only when role == PATIENT (validated in AuthService).
    @Min(value = 0, message = "Age must be a positive number")
    private Integer age;

    // Required only when role == PATIENT (validated in AuthService).
    private String gender;
}
