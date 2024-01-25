package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.OTPs;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.OtpType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.OtpRepo;
import com.fyp.health_sync.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;

    private static String generateOTP() {
        // Generating a random 4-digit OTP
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Generates a number between 1000 and 9999
        return String.valueOf(otp);
    }

    public String getOtp(String email, OtpType type) throws BadRequestException, InternalServerErrorException {
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
       try {
           String otp = generateOTP();
           otpRepo.save(OTPs.builder()
                   .otp(otp)
                   .otpType(type)
                   .email(email)
                   .createdAt(LocalDateTime.now())
                   .user(user)
                   .expiresAt(LocalDateTime.now().plusMinutes(5))
                   .build()
           );
           return otp;
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }


    public Boolean validateOtp(String email, String otp, OtpType type) {
        OTPs otps = otpRepo.findByEmailAndOtpTypeAndOtp(email, type, otp);
        if (otps != null && otps.getExpiresAt().isAfter(LocalDateTime.now())) {
            otps.setIsExpired(true);
            otpRepo.save(otps);
            return true;
        } else {
            return false;
        }
    }




}
