package com.example.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class VerificationCode {
    @Id

    private String email;
    private String code;
    private LocalDateTime expiryDate;

    // Getters and setters
}
