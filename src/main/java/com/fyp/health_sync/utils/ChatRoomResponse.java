package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String lastMessageAt;
    private String messageType;
    private String createdAt;
    private String deletedAt;
    private DoctorResponse doctor;
    private UserResponse user;

    public ChatRoomResponse castToResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .lastMessage(chatRoom.getLastMessage())
                .senderId(chatRoom.getSenderId())
                .deletedBy(chatRoom.getDeletedBy() != null ? chatRoom.getDeletedBy().toString() : null)
                .lastMessageAt(chatRoom.getLastMessageAt()!=null?chatRoom.getLastMessageAt().toString():null)
                .messageType(chatRoom.getMessageType() != null ? chatRoom.getMessageType().toString() : null)
                .createdAt(chatRoom.getCreatedAt().toString())
                .deletedAt(chatRoom.getDeletedAt()!=null?chatRoom.getDeletedAt().toString():null)
                .doctor(new DoctorResponse().castToResponse(chatRoom.getDoctor()))
                .user(new UserResponse().castToResponse(chatRoom.getUser()))
                .build();
    }
}
