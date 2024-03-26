package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.DataRemovalRequest;
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
public class RemovalRequestResponse {
    private UUID id;

    private UserResponse user;

    private String reason;

    private String type;

    private boolean isAccepted;

    private boolean isRejected;

    private LocalDateTime createdAt;

    public RemovalRequestResponse castToResponse(DataRemovalRequest request){
        return RemovalRequestResponse.builder()
                .id(request.getId())
                .user(new UserResponse().castToResponse(request.getUser()))
                .reason(request.getReason())
                .type(request.getType())
                .isAccepted(request.isAccepted())
                .isRejected(request.isRejected())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
