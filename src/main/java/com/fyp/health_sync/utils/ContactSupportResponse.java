package com.fyp.health_sync.utils;


import com.fyp.health_sync.entity.ContactSupport;
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
public class ContactSupportResponse {

    private UUID id;
    private String email;
    private String message;
    private LocalDateTime createdAt;
    private UserResponse user;
    private String responseMessage;

    public ContactSupportResponse castToResponse(ContactSupport contactSupport) {
        return ContactSupportResponse.builder()
                .id(contactSupport.getId())
                .email(contactSupport.getEmail())
                .message(contactSupport.getMessage())
                .createdAt(contactSupport.getCreatedAt())
                .responseMessage(contactSupport.getResponseMessage())
                .user(new UserResponse().castToResponse(contactSupport.getUser()))
                .build();
    }
}
