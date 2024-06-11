package com.example.project.controller;

import com.example.project.service.EmailService;
import com.example.project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PasswordResetController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        String resetToken = passwordResetService.createToken(email);
        String resetUrl = "http://your-domain.com/reset-password?token=" + resetToken;
        emailService.sendEmail(email, "Password Reset Request", "Click the link to reset your password: " + resetUrl);
        return "Password reset email sent.";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token) {
        if (passwordResetService.validateToken(token)) {
            return "Valid token. Show form to reset password.";
        } else {
            return "Invalid or expired token.";
        }
    }

    @PostMapping("/reset-password")
        public String resetPassword(@RequestParam String token, @RequestParam String newPassword) {
            if (passwordResetService.validateToken(token)) {
                // Your logic to reset the password (update user entity with new password)
                return "Password has been reset successfully.";
            } else {
                return "Invalid or expired token.";
            }
        }
    }
