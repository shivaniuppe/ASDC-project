package com.example.project.controller;

import com.example.project.model.User;
import com.example.project.model.VerificationRequest;
import com.example.project.service.UserService;
import com.example.project.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        try {
            if (!user.getEmail().endsWith("@dal.ca")) {
                return ResponseEntity.badRequest().body("Email must be a @dal.ca address");
            }
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            userService.registerUser(user);
            // Send verification code logic
            return ResponseEntity.ok("User registered successfully. Please check your email for verification code.");
        } catch (Exception e) {
            logger.error("Error registering user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(Map.of("token", jwt));
        } catch (Exception e) {
            logger.error("Error logging in user: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody VerificationRequest request) {
        logger.info("Received verification request: {}", request);
        try {
            logger.info("Verifying user with email: {} and code: {}", request.getEmail(), request.getCode());
            boolean isVerified = userService.verifyUser(request.getEmail(), request.getCode());
            if (isVerified) {
                logger.info("Verification successful for email: {}", request.getEmail());
                return ResponseEntity.ok("User verified successfully");
            } else {
                logger.warn("Verification failed for email: {} with code: {}", request.getEmail(), request.getCode());
                return ResponseEntity.badRequest().body("Invalid verification code or code has expired");
            }
        } catch (Exception e) {
            logger.error("Error verifying user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying user.");
        }
    }
}
