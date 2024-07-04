package com.example.project.controller;

import com.example.project.model.User;
import com.example.project.model.VerificationRequest;
import com.example.project.service.UserService;
import com.example.project.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignup_Success() {
        User user = new User();
        user.setEmail("test@dal.ca");
        user.setPassword("password");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doAnswer(invocation -> null).when(userService).registerUser(any(User.class));

        ResponseEntity<?> response = authController.signup(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully. Please check your email for verification code.", response.getBody());
    }


    @Test
    public void testSignup_InvalidEmail() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");

        ResponseEntity<?> response = authController.signup(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email must be a @dal.ca address", response.getBody());
    }

    @Test
    public void testSignup_Exception() {
        User user = new User();
        user.setEmail("test@dal.ca");
        user.setPassword("password");

        when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException());

        ResponseEntity<?> response = authController.signup(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error registering user.", response.getBody());
    }

    @Test
    public void testLogin_Success() {
        User user = new User();
        user.setEmail("test@dal.ca");
        user.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@dal.ca");
        when(jwtUtils.generateToken(anyString())).thenReturn("jwtToken");

        ResponseEntity<?> response = authController.login(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("token", "jwtToken"), response.getBody());
    }

    @Test
    public void testLogin_InvalidCredentials() {
        User user = new User();
        user.setEmail("test@dal.ca");
        user.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException());

        ResponseEntity<?> response = authController.login(user);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    public void testVerifyEmail_Success() {
        VerificationRequest request = new VerificationRequest();
        request.setEmail("test@dal.ca");
        request.setCode("123456");

        when(userService.verifyUser(anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.verifyEmail(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User verified successfully", response.getBody());
    }

    @Test
    public void testVerifyEmail_InvalidCode() {
        VerificationRequest request = new VerificationRequest();
        request.setEmail("test@dal.ca");
        request.setCode("123456");

        when(userService.verifyUser(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.verifyEmail(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid verification code or code has expired", response.getBody());
    }

    @Test
    public void testVerifyEmail_Exception() {
        VerificationRequest request = new VerificationRequest();
        request.setEmail("test@dal.ca");
        request.setCode("123456");

        when(userService.verifyUser(anyString(), anyString())).thenThrow(new RuntimeException());

        ResponseEntity<?> response = authController.verifyEmail(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error verifying user.", response.getBody());
    }
}
