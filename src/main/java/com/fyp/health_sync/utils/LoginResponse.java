package com.fyp.health_sync.utils;

import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse extends SuccessResponse {
    private String token;
    private boolean isVerified;
    private AuthType authType;
    private UserStatus status;
    private String role;
}
