package com.fyp.health_sync.utils;


import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private boolean verified;
    private UserStatus accountStatus;
    private Integer fee;
    private String avatar;
    private String address;
    private double latitude;
    private double longitude;
    private String khaltiId;
    private Boolean textNotification;
    private double avgRatings = 0;
    private int ratingCount = 0;
    private boolean isApproved;
    private boolean isFavorite;


    public DoctorResponse castToResponse(Users doctor) {
        if (doctor == null) {
            return null;
        }
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .email(doctor.getEmail())
                .authType(doctor.getAuthType())
                .createdAt(doctor.getCreatedAt().toString())
                .updatedAt(doctor.getUpdatedAt() != null ? doctor.getUpdatedAt().toString() : null)
                .deletedAt(doctor.getDeletedAt() != null ? doctor.getDeletedAt().toString() : null)
                .fee(doctor.getFee())
                .address(doctor.getAddress())
                .speciality(doctor.getSpeciality() != null ? doctor.getSpeciality().getName() : null)
                .latitude(doctor.getLatitude())
                .longitude(doctor.getLongitude())
                .experience(doctor.getExperience())
                .textNotification(doctor.isTextNotification())
                .isApproved(doctor.getApproved() != null ? doctor.getApproved() : false)
                .accountStatus(doctor.getStatus())
                .verified(doctor.getIsVerified())
                .khaltiId(doctor.getKhaltiId())
                .avatar(doctor.getProfilePicture() != null ? "/files/get-avatar/" + doctor.getId() : null)
                .build();
    }

}
