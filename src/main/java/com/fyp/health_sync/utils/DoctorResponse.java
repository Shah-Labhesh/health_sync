package com.fyp.health_sync.utils;


import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {

    private UUID id;
    private String name;
    private String email;
    private boolean isPopular;
    private String speciality;
    private String experience;
    private AuthType authType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private boolean isVerified;
    private UserStatus accountStatus;
    private Integer fee;
    private String avatar;
    private String address;
    private double latitude;
    private double longitude;
    private String khaltiId;

    private boolean isApproved;
    private boolean isFavorite;

    public DoctorResponse castToResponse(Doctors doctor){
        if (doctor == null) {
            return null;
        }
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .authType(doctor.getAuthType())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .deletedAt(doctor.getDeletedAt())
                .fee(doctor.getFee())
                .address(doctor.getAddress())
                .speciality(doctor.getSpeciality() != null ? doctor.getSpeciality().getName() : null)
                .latitude(doctor.getLatitude())
                .longitude(doctor.getLongitude())
                .experience(doctor.getExperience())
                .isApproved(doctor.getApproved())
                .accountStatus(doctor.getAccountStatus())
                .isVerified(doctor.getIsVerified())
                .avatar(doctor.getImage() != null ? "get-avatar/" + doctor.getId() : null)
                .build();
    }

}
