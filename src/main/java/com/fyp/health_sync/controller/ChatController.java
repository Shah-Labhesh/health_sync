package com.fyp.health_sync.controller;

import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.entity.Notification;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @GetMapping("/my-rooms")
    @SendTo("/topic/my-rooms")
    public ResponseEntity<?> getChatRoom() throws BadRequestException, InternalServerErrorException {
        return messageService.getMyChatRooms();
    }

    @MessageMapping("/create-room")
    @SendTo("/topic/create-room")
    public ResponseEntity<?> createChatRoom(@Payload UUID doctorId) throws BadRequestException {
        return messageService.createChatRoom(doctorId);
    }

    @GetMapping("get-room")
    @SendTo("/topic/get-room")
    public ResponseEntity<?> getChatRoom(@Payload UUID roomId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.getChatRoom(roomId);
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload UUID roomId, @Payload String message) throws BadRequestException {
        Message message1 = messageService.createMessage(roomId, message);
        messagingTemplate.convertAndSend("/topic/" + roomId, Notification.builder()
                .body("You have a new message")
                .title("New Message")
                .targetId(roomId)
                .createdAt(message1.getCreatedAt())
                .isRead(false)
                .type(NotificationType.CHAT)
                .build());
    }

    @GetMapping("get-messages")
    @SendTo("/topic/get-messages")
    public ResponseEntity<?> getMessages(@Payload UUID roomId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.getRoomMessages(roomId);
    }

    @GetMapping("delete-room")
    @SendTo("/topic/delete-room")
    public ResponseEntity<?> deleteRoom(@Payload UUID roomId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.deleteMyChatRoom(roomId);
    }

    @GetMapping("delete-message")
    @SendTo("/topic/delete-message")
    public ResponseEntity<?> deleteMessage(@Payload UUID messageId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.deleteMessage(messageId);
    }



}
