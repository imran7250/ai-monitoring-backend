package com.imran.aimonitoring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.PasswordResetToken;
import com.imran.aimonitoring.entity.User;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    
    void deleteByUser(User user);
}