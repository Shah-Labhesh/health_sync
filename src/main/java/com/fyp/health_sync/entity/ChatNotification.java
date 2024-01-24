package com.fyp.health_sync.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatNotification {

    private UUID chatId;
    private UUID senderId;
    private UUID receiverId;
    private String message;
}
