package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private UUID id;
    private String message;

    private String file;
    private UUID senderId;
    private UUID receiverId;
    private boolean me;
    private MessageType messageType;
    private String createdAt;
    private String deletedAt;


    private ChatRoomResponse chatRoom;

    public MessageResponse castToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .file(message.getFile() != null ? "/files/message-file/" + message.getId() : null)
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .messageType(message.getMessageType())

                .createdAt(message.getCreatedAt().toString())
                .deletedAt(message.getDeletedAt() != null ? message.getDeletedAt().toString() : null)
                .chatRoom(new ChatRoomResponse().castToResponse(message.getChatRoom()))
                .build();
    }
}
