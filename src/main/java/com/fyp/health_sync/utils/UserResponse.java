package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserRole;
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
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String avatar;
    private String createdAt;
    private Boolean verified;
    private UserStatus accountStatus;
    private UserRole role;
    private AuthType authType;
    private Boolean textNotification;
    private String updatedAt;
    private String deletedAt;

    public UserResponse castToResponse(Users user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder().id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .authType(user.getAuthType())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .deletedAt(user.getDeletedAt() != null ? user.getDeletedAt().toString() : null)
                .accountStatus(user.getStatus())
                .verified(user.getIsVerified())
                .textNotification(user.isTextNotification())
                .avatar(user.getProfilePicture() != null ? "/files/get-avatar/" + user.getId() : null)
                .role(user.getRole())
                .build();
    }
}
