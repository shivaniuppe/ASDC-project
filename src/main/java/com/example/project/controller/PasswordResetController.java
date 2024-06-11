package com.example.project.controller;


import com.example.project.entity.PasswordResetToken;
import com.example.project.model.User;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.project.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository; // Assuming UserRepository is your repository interface

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        // Generate a reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        // Save the reset token to the database
        passwordResetTokenRepository.save(resetToken);

        // Create the reset URL
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;

        // Send the email
        emailService.sendEmail(email, "Password Reset Request", "Click the link to reset your password: " + resetUrl);

        return "Password reset email sent.";
    }
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword) {
        // Validate the token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (!resetToken.getEmail().equals(email)) {
            throw new RuntimeException("Invalid email address");
        }

        // Fetch the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encode the new password
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Update the user's password
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Delete the token after a successful password reset
        passwordResetTokenRepository.delete(resetToken);

        return "Password reset successfully.";
    }
}
