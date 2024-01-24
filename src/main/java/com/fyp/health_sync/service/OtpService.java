package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.OTPs;
import com.fyp.health_sync.enums.OtpType;
import com.fyp.health_sync.repository.OtpRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepo otpRepo;

    private static String generateOTP() {
        // Generating a random 4-digit OTP
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Generates a number between 1000 and 9999
        return String.valueOf(otp);
    }

    public String getOtp(String email, OtpType type) {
        // Generate OTP
        String otp = generateOTP();
        // Save OTP in DB
        otpRepo.save(OTPs.builder()
                .otp(otp)
                .otpType(type)
                .email(email)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build()
        );
        return otp;
    }


    public Boolean validateOtp(String email, String otp, OtpType type) {
        // Get OTP from DB
        OTPs otps = otpRepo.findByEmailAndOtpTypeAndOtp(email, type, otp);
        // Check if OTP is valid
        if (otps != null && otps.getExpiresAt().isAfter(LocalDateTime.now())) {
            // Mark OTP as expired
            otps.setIsExpired(true);
            otpRepo.save(otps);
            return true;
        } else {
            return false;
        }
    }




}
