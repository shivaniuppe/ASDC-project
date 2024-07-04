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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

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

}
