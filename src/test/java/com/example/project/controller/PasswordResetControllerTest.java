package com.example.project.controller;

import com.example.project.entity.PasswordResetToken;
import com.example.project.model.User;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    @InjectMocks
    private PasswordResetController passwordResetController;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testForgotPassword() {
        String email = "test@example.com";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);

        String response = passwordResetController.forgotPassword(email);

        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendEmail(eq(email), eq("Password Reset Request"), contains("Click the link to reset your password:"));
        assertEquals("Password reset email sent.", response);
    }

    @Test
    void testResetPassword_Success() {
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        User user = new User();
        user.setEmail(email);
        user.setPassword("oldPassword");

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        String response = passwordResetController.resetPassword(email, token, newPassword);

        verify(passwordResetTokenRepository, times(1)).findByToken(token);
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);
        verify(passwordResetTokenRepository, times(1)).delete(resetToken);

        assertEquals("Password reset successfully.", response);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void testResetPassword_InvalidToken() {
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                passwordResetController.resetPassword(email, token, newPassword));

        assertEquals("Invalid or expired token", exception.getMessage());
        verify(passwordResetTokenRepository, times(1)).findByToken(token);
        verify(userRepository, times(0)).findByEmail(anyString());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
        verify(passwordResetTokenRepository, times(0)).delete(any(PasswordResetToken.class));
    }

    @Test
    void testResetPassword_InvalidEmail() {
        String email = "test@example.com";
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail("other@example.com");
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                passwordResetController.resetPassword(email, token, newPassword));

        assertEquals("Invalid email address", exception.getMessage());
        verify(passwordResetTokenRepository, times(1)).findByToken(token);
        verify(userRepository, times(0)).findByEmail(anyString());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
        verify(passwordResetTokenRepository, times(0)).delete(any(PasswordResetToken.class));
    }
}
