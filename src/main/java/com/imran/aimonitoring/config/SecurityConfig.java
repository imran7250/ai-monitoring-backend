package com.imran.aimonitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.imran.aimonitoring.security.JwtAuthFilter;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                    // ✅ PUBLIC — only auth and password reset
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/test-mail").permitAll()

                    // ✅ INTERNAL — baseline computation endpoints
                    //    In production, protect these with an API key or remove entirely
                    .requestMatchers("/internal/**").permitAll()

                    // ✅ ADMIN ONLY
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // ✅ EVERYTHING ELSE REQUIRES A VALID JWT
                    //    This covers /api/notifications/**, /api/anomalies/**,
                    //    /api/services/**, /api/projects/**, /api/alerts/**,
                    //    /api/incidents/**, /api/metrics/**, /api/users/**,
                    //    /api/dashboard/** — all secured
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();   
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}