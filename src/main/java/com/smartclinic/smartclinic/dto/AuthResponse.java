package com.smartclinic.smartclinic.dto;

import com.smartclinic.smartclinic.entity.Role;
import lombok.Getter;
import lombok.Setter;

/**
 * Response body returned from both POST /auth/register and POST /auth/login.
 * Carries the JWT plus a little context the client will likely want
 * immediately (id, name, role) without having to decode the token itself.
 */
@Getter
@Setter
public class AuthResponse {

    private String token;
    private final String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public AuthResponse(String token, Long userId, String name, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
