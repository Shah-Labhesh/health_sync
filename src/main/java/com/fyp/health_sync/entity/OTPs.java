package com.fyp.health_sync.entity;


import com.fyp.health_sync.enums.OtpType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "otps")
public class OTPs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String otp;
    @Enumerated (EnumType.STRING)
    private OtpType otpType;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", referencedColumnName = "id")
    private Users user;

}
