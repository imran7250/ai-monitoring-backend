package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.imran.aimonitoring.dto.LoginRequest;
import com.imran.aimonitoring.dto.RegisterRequest;
import com.imran.aimonitoring.entity.PasswordResetToken;
import com.imran.aimonitoring.entity.User;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.PasswordResetTokenRepository;
import com.imran.aimonitoring.repository.UserRepository;
import com.imran.aimonitoring.security.CustomUserDetails;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // =========================
    // REGISTER
    // =========================
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already registered";
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_DEVELOPER")
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    // =========================
    // LOGIN
    // =========================
    public User login(LoginRequest request) {

        // ✅ FIXED
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    // =========================
    // GET ALL USERS (ADMIN)
    // =========================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // =========================
    // GET USER BY ID
    // =========================
    public User getById(Long id) {
        // ✅ FIXED
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // =========================
    // UPDATE SELF
    // =========================
    public User updateSelf(Long id, String name, String password) {

        User user = getById(id);

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(user);
    }

    // =========================
    // ADMIN UPDATE USER
    // =========================
    public User adminUpdateUser(Long id, String name, String role) {

        User user = getById(id);

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (role != null && !role.isBlank()) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    // =========================
    // DELETE USER (ADMIN)
    // =========================
//    public void deleteUser(Long id) {
//
//        CustomUserDetails currentUser =
//                (CustomUserDetails) SecurityContextHolder
//                        .getContext()
//                        .getAuthentication()
//                        .getPrincipal();
//
//        if (currentUser.getId().equals(id)) {
//            throw new RuntimeException("Admin cannot delete themselves");
//        }
//
//        // ✅ FIXED
//        User userToDelete = userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User", id));
//
//        if ("ROLE_ADMIN".equals(userToDelete.getRole())) {
//            long adminCount = userRepository.countByRole("ROLE_ADMIN");
//            if (adminCount <= 1) {
//                throw new RuntimeException(
//                    "Cannot delete the last admin account. " +
//                    "Promote another user to admin first."
//                );
//            }
//        }
//
//        userRepository.deleteById(id);
//    }

    public void deleteUser(Long id) {

        CustomUserDetails currentUser =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (currentUser.getId().equals(id)) {
            throw new RuntimeException("Admin cannot delete themselves");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if ("ROLE_ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.countByRole("ROLE_ADMIN");
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete last admin");
            }
        }

        // 🔥 NEW FIX — BLOCK DELETE IF USER HAS DATA
        boolean hasProjects = userRepository.existsUserWithProjects(id);

        if (hasProjects) {
            throw new RuntimeException(
                "Cannot delete user. User owns projects/services."
            );
        }

        userRepository.delete(user);
    }
    // =========================
    // CHANGE PASSWORD
    // =========================
    public void changePassword(Long userId, String currentPassword, String newPassword) {

        User user = getById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // =========================
    // CREATE PASSWORD RESET TOKEN
    // =========================
    @Transactional
    public String createPasswordResetToken(String email) {

        // ✅ FIXED
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plusMinutes(15)
        );

        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    // =========================
    // RESET PASSWORD
    // =========================
    @Transactional
    public void resetPassword(String token, String newPassword) {

        // ✅ FIXED
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token expired");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.deleteByUser(user);
    }
}