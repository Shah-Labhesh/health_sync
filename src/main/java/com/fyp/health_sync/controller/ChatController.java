package com.fyp.health_sync.controller;

import com.fyp.health_sync.entity.ChatNotification;
import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.service.ChatRoomService;
import com.fyp.health_sync.service.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    @GetMapping("/my-rooms")
    @SendTo("/topic/my-rooms")
    public ResponseEntity<?> getChatRoom() throws BadRequestException {
        return chatRoomService.getMyChatRooms();
    }

    @MessageMapping("/create-room")
    @SendTo("/topic/create-room")
    public ResponseEntity<?> createChatRoom(@Payload UUID doctorId) throws BadRequestException {
        return chatRoomService.createChatRoom(doctorId);
    }

    @GetMapping("get-room")
    @SendTo("/topic/get-room")
    public ResponseEntity<?> getChatRoom(@Payload UUID roomId) throws BadRequestException {
        return chatRoomService.getChatRoom(roomId);
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload UUID roomId, @Payload String message) throws BadRequestException {
        Message message1 = messageService.createMessage(roomId, message);
        messagingTemplate.convertAndSend("/topic/" + roomId, ChatNotification.builder()
                .message(message1.getMessage())
                .senderId(message1.getSenderId())
                .receiverId(message1.getReceiverId())
                .chatId(message1.getId())
                .build());
    }
}
