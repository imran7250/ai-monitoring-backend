package com.imran.aimonitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// ✅ BUG FIX #3 — This is a BRAND NEW FILE you need to create.
//
//    The BCryptPasswordEncoder @Bean was previously inside SecurityConfig.java.
//    That caused a circular dependency:
//
//      SecurityConfig → JwtAuthFilter → CustomUserDetailsService
//      UserService    → BCryptPasswordEncoder (defined in SecurityConfig)
//      SecurityConfig → JwtAuthFilter   ← CYCLE → Spring startup crash
//
//    Solution: Move the bean to this standalone class which has zero dependencies.
//    Spring will find it automatically — no other code changes needed.

@Configuration
public class PasswordConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}