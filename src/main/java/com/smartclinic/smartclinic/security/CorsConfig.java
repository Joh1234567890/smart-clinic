package com.smartclinic.smartclinic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Allows the React frontend (served from a different origin during local
 * development, e.g. http://localhost:5173 via Vite or http://localhost:3000
 * via Create React App) to call this API.
 *
 * Without this, the browser blocks every cross-origin request before it
 * even reaches Spring Security, regardless of how SecurityConfig's rules
 * are set up - CORS is enforced by the browser, ahead of any
 * authentication/authorization on the server.
 *
 * This bean is picked up automatically by SecurityConfig's
 * `.cors(...)` configuration on the HttpSecurity chain.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Common local dev ports for Vite (5173) and Create React App (3000).
        // Add your deployed frontend's origin here too once you have one -
        // wildcard origins ("*") cannot be combined with allowCredentials.
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
