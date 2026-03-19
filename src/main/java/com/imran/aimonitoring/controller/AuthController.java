//package com.imran.aimonitoring.controller;
//
//import java.util.Map;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.imran.aimonitoring.dto.LoginRequest;
//import com.imran.aimonitoring.dto.RegisterRequest;
//import com.imran.aimonitoring.security.JwtUtil;
//import com.imran.aimonitoring.service.EmailSenderService;
//import com.imran.aimonitoring.service.UserService;
//
//import jakarta.validation.Valid;
//
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final UserService userService;
//    private final JwtUtil jwtUtil;
//    private final EmailSenderService emailSenderService;
//
//    public AuthController(EmailSenderService emailSenderService,UserService userService, JwtUtil jwtUtil) {
//    	this.emailSenderService = emailSenderService;
//    	this.userService = userService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    // ✅ REGISTER (NO CHANGE IN LOGIC)
//    
//    @PostMapping("/register")
//    public String register(@Valid @RequestBody RegisterRequest request) {
//        return userService.register(request);   
//     
//    }
//
//    // ✅ LOGIN → RETURNS JWT
////    @PostMapping("/login")
////    public String login(@RequestBody LoginRequest request) {
////
////        // 1️⃣ Validate credentials
////        userService.login(request);
////
////        // 2️⃣ Generate JWT
////        return jwtUtil.generateToken(request.getEmail());
////    }
//
//    
//    @PostMapping("/login")
//    public Object login(@RequestBody LoginRequest request) {
//
//        // validate credentials
////        var user = userService.login(request);
//
//        // generate token
////        String token = jwtUtil.generateToken(user.getEmail());
//        
//        var user = userService.login(request);
//
//        String token = jwtUtil.generateToken(
//                user.getEmail(),
//                user.getRole()
//        );
//
//        // return structured response
//        return java.util.Map.of(
//                "token", token,
//                "user", java.util.Map.of(
//                        "id", user.getId(),
//                        "name", user.getName(),
//                        "email", user.getEmail(),
//                        "role", user.getRole()
//                )
//        );
//    }
//    
//    // 🔒 TEST ENDPOINT (JWT REQUIRED)
//    @GetMapping("/test")
//    public String test() {     
//        return "AUTH OK - JWT VALID";
//    }
//    
//    
//    // for forgot password
////    @PostMapping("/forgot-password")
////    public Object forgotPassword(@RequestBody Map<String,String> body) {
////
////        String email = body.get("email");
////
////        String token = userService.createPasswordResetToken(email);
////
////        String resetLink =
////            "http://localhost:5173/reset-password?token=" + token;
////
////        return Map.of(
////            "message","Reset link generated",
////            "resetLink",resetLink
////        );
////    }
////    
//    
//    @PostMapping("/forgot-password")
//    public Object forgotPassword(@RequestBody Map<String,String> body) {
//
//        String email = body.get("email");
//
//        String token = userService.createPasswordResetToken(email);
//
//        String resetLink =
//            "http://localhost:5173/reset-password?token=" + token;
//
//        emailSenderService.sendEmail(
//                email,
//                "Password Reset Request",
//                "Click the link to reset your password:\n\n"
//                        + resetLink +
//                "\n\nThis link expires in 15 minutes."
//        );
//
//        return Map.of(
//            "message","If the email exists, reset instructions have been sent"
//        );
//    }
//    
//    
//    //Reset password
//   // @PostMapping("/reset-password")
////    public Object resetPassword(@RequestBody Map<String,String> body) {
////
////        String token = body.get("token");
////        String newPassword = body.get("newPassword");
////
////        userService.resetPassword(token,newPassword);
////
////        return Map.of(
////            "message","Password reset successful"
////        );
////    }
//    
//    
//    @PostMapping("/reset-password")
//    public Object resetPassword(@RequestBody Map<String,String> body) {
//
//        String token = body.get("token");
//        String newPassword = body.get("newPassword");
//
//        userService.resetPassword(token,newPassword);
//
//        return Map.of(
//            "message","Password reset successful"
//        );
//    }
//    
//}

package com.imran.aimonitoring.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.dto.LoginRequest;
import com.imran.aimonitoring.dto.RegisterRequest;
import com.imran.aimonitoring.security.JwtUtil;
import com.imran.aimonitoring.service.EmailSenderService;
import com.imran.aimonitoring.service.UserService;

import jakarta.validation.Valid;

// ✅ IMPROVEMENT #6 — Added /auth/refresh endpoint
//    Before: Token expires after 1 hour, user silently gets logged out
//    After:  Frontend can call /auth/refresh before expiry to get a new token
//            User stays logged in without re-entering credentials

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailSenderService emailSenderService;

    public AuthController(EmailSenderService emailSenderService,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.emailSenderService = emailSenderService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest request) {

        var user = userService.login(request);

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return Map.of(
                "token", token,
                "user", Map.of(
                        "id",    user.getId(),
                        "name",  user.getName(),
                        "email", user.getEmail(),
                        "role",  user.getRole()
                )
        );
    }

    // ✅ IMPROVEMENT #6 — NEW: Refresh token endpoint
    //    Call this from frontend before the 1-hour expiry to get a fresh token.
    //    Frontend usage:
    //      const res = await axios.post('/auth/refresh', {}, {
    //          headers: { Authorization: `Bearer ${currentToken}` }
    //      });
    //      localStorage.setItem('token', res.data.token);
    @PostMapping("/refresh")
    public Object refresh(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }

        String oldToken = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(oldToken)) {
            throw new RuntimeException("Token is invalid or expired — please log in again");
        }

        String email = jwtUtil.extractEmail(oldToken);
        String role  = jwtUtil.extractRole(oldToken);

        String newToken = jwtUtil.generateToken(email, role);

        return Map.of("token", newToken);
    }

    // ✅ TEST ENDPOINT
    @GetMapping("/test")
    public String test() {
        return "AUTH OK - JWT VALID";
    }

    // ✅ FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public Object forgotPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        String token = userService.createPasswordResetToken(email);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        emailSenderService.sendEmail(
                email,
                "Password Reset Request",
                "Click the link to reset your password:\n\n"
                        + resetLink
                        + "\n\nThis link expires in 15 minutes."
        );

        return Map.of("message", "If the email exists, reset instructions have been sent");
    }

    // ✅ RESET PASSWORD
    @PostMapping("/reset-password")
    public Object resetPassword(@RequestBody Map<String, String> body) {

        String token       = body.get("token");
        String newPassword = body.get("newPassword");

        userService.resetPassword(token, newPassword);

        return Map.of("message", "Password reset successful");
    }
}
