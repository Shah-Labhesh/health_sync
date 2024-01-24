package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.OTPs;
import com.fyp.health_sync.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepo extends JpaRepository<OTPs, Long> {
    OTPs findByEmailAndOtpTypeAndOtp(String email, OtpType type, String otp);
}
