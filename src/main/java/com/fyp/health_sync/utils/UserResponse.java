package com.fyp.health_sync.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
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
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String profilePicture;
    private LocalDateTime createdAt;
    private Boolean isVerified;
    private UserStatus status;
    private UserRole role;
    private AuthType authType;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public UserResponse castToResponse(Users user){
        if (user == null) {
            return null;
        }
        return UserResponse.builder().id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .authType(user.getAuthType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .status(user.getStatus())
                .isVerified(user.getIsVerified())
                .profilePicture(user.getProfilePicture() != null ? "get-avatar/" + user.getId() : null)
                .role(user.getRole())
                .build();
    }
}
