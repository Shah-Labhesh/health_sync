package com.fyp.health_sync.controller;

import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.MessageService;
import com.fyp.health_sync.utils.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocket {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @GetMapping("/get-room")
    @SendTo("/topic/get-room")
    public ResponseEntity<?> getChatRoom(@Payload UUID roomId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.getChatRoom(roomId);
    }

    @MessageMapping("/message")
    public void processMessage(@Payload Map<String, String> payload) throws BadRequestException, InternalServerErrorException {
        UUID roomId = UUID.fromString(payload.get("roomId"));
        String message = payload.get("message");
        String token = payload.get("token");
        MessageResponse message1 = messageService.createMessage(roomId, message,token);
        messagingTemplate.convertAndSend("/topic/"+roomId, message1);
    }

    @MessageMapping("/get-messages")
    @SendTo("/topic/get-messages")
    public ResponseEntity<?> getMessages(@Payload Map<String, String> payload) throws BadRequestException, ForbiddenException {
        UUID roomId = UUID.fromString(payload.get("roomId"));
        String token = payload.get("token");
        return messageService.getRoomMessages(roomId,token);
    }

    @GetMapping("/delete-room")
    @SendTo("/topic/delete-room")
    public ResponseEntity<?> deleteRoom(@Payload UUID roomId) throws BadRequestException,  InternalServerErrorException {
        return messageService.deleteMyChatRoom(roomId);
    }

    @GetMapping("/delete-message")
    @SendTo("/topic/delete-message")
    public ResponseEntity<?> deleteMessage(@Payload UUID messageId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return messageService.deleteMessage(messageId);
    }

}
