package com.example.project.service;

import com.example.project.model.User;
import com.example.project.model.VerificationCode;
import com.example.project.repository.UserRepository;
import com.example.project.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

   // @Autowired
    //private VerificationService verificationService;

    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {
        User registeredUser = userRepository.save(user);

        String verificationCode = generateVerificationCode();
        VerificationCode code = new VerificationCode();
        code.setEmail(user.getEmail());
        code.setCode(verificationCode);
        code.setExpiryDate(LocalDateTime.now().plusHours(1));
        verificationCodeRepository.save(code);

        String subject = "Verify your email";
        String text = "Your verification code is " + verificationCode;
        emailService.sendEmail(user.getEmail(), subject, text);

        return registeredUser;
    }

    public boolean verifyUser(String email, String code) {
        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByEmailAndCode(email, code);
        return verificationCode.isPresent() && verificationCode.get().getExpiryDate().isAfter(LocalDateTime.now());
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }



    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public void sendResetPasswordEmail(String email) {
        // Generate reset password token
        String token = UUID.randomUUID().toString();
        VerificationCode code = new VerificationCode();
        code.setEmail(email);
        code.setCode(token);
        code.setExpiryDate(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        verificationCodeRepository.save(code);

        // Send reset password email
        String subject = "Reset your password";
        String text = "To reset your password, please click the link below:\n" +
                "http://localhost:3000/reset-password?token=" + token + "&email=" + email;
        emailService.sendEmail(email, subject, text);
    }


    public void saveUser(User user) {
        userRepository.save(user);
    }

}
