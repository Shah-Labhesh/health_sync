package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.ChatRoom;
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
public class ChatRoomResponse {
    private UUID id;
    private String lastMessage;
    private UUID senderId;
    private String deletedBy;
    private LocalDateTime lastMessageAt;
    private String messageType;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private DoctorResponse doctor;
    private UserResponse user;

    public ChatRoomResponse castToResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .lastMessage(chatRoom.getLastMessage())
                .senderId(chatRoom.getSenderId())
                .deletedBy(chatRoom.getDeletedBy().toString())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .messageType(chatRoom.getMessageType().toString())
                .createdAt(chatRoom.getCreatedAt())
                .deletedAt(chatRoom.getDeletedAt())
                .doctor(new DoctorResponse().castToResponse(chatRoom.getDoctor()))
                .user(new UserResponse().castToResponse(chatRoom.getUser()))
                .build();
    }
}
