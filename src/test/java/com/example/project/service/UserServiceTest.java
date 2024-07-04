package com.example.project.service;

import com.example.project.model.User;
import com.example.project.model.VerificationCode;
import com.example.project.repository.UserRepository;
import com.example.project.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @Spy
    private Random random = new Random();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        User user = new User();
        user.setEmail("test@dal.ca");
        user.setPassword("password");

        when(userRepository.save(any(User.class))).thenReturn(user);
        doAnswer(invocation -> {
            VerificationCode code = invocation.getArgument(0);
            code.setCode("123456");
            code.setExpiryDate(LocalDateTime.now().plusHours(1));
            return code;
        }).when(verificationCodeRepository).save(any(VerificationCode.class));

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals(user.getEmail(), registeredUser.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));
        verify(emailService, times(1)).sendEmail(eq(user.getEmail()), eq("Verify your email"), anyString());
    }

    @Test
    public void testVerifyUser_Success() {
        String email = "test@dal.ca";
        String code = "123456";

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(verificationCodeRepository.findByEmailAndCode(anyString(), anyString())).thenReturn(Optional.of(verificationCode));

        boolean isVerified = userService.verifyUser(email, code);

        assertTrue(isVerified);
        verify(verificationCodeRepository, times(1)).findByEmailAndCode(email, code);
    }

    @Test
    public void testVerifyUser_Failure() {
        String email = "test@dal.ca";
        String code = "123456";

        when(verificationCodeRepository.findByEmailAndCode(anyString(), anyString())).thenReturn(Optional.empty());

        boolean isVerified = userService.verifyUser(email, code);

        assertFalse(isVerified);
        verify(verificationCodeRepository, times(1)).findByEmailAndCode(email, code);
    }
}
