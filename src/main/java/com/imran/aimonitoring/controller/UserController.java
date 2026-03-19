


package com.imran.aimonitoring.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.entity.User;
import com.imran.aimonitoring.security.CustomUserDetails;
import com.imran.aimonitoring.service.UserService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =============================
    // SELF ENDPOINT
    // =============================

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me")
    public Object getCurrentUser() {

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User user = userService.getById(userDetails.getId());

        return mapUser(user);
    }
    
    
    
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/users/me/password")
    public String changePassword(@RequestBody Map<String, String> body) {

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        userService.changePassword(
                userDetails.getId(),
                currentPassword,
                newPassword
        );

        return "Password updated successfully";
    }
    

    // =============================
    // ADMIN ENDPOINTS
    // =============================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<?> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(this::mapUser)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // =============================
    // PRIVATE SAFE MAPPER
    // =============================

    private Object mapUser(User u) {
        return java.util.Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole(),
                "createdAt", u.getCreatedAt()
        );
    }
    
    
    //Self Update ........................
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/users/me")
    public Object updateSelf(@RequestBody java.util.Map<String, String> body) {

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        String name = body.get("name");
        String password = body.get("password");

        User updated = userService.updateSelf(
                userDetails.getId(),
                name,
                password
        );

        return mapUser(updated);
    }
    
    //Admin Update...............
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}")
    public Object adminUpdateUser(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {

        String name = body.get("name");
        String role = body.get("role");

        User updated = userService.adminUpdateUser(id, name, role);

        return mapUser(updated);
    }
    
    
    
    
    
}

