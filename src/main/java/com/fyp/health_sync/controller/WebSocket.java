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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
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
    public void processMessage(@ModelAttribute @Payload Map<String, Object> payload) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        UUID roomId = UUID.fromString((String) payload.get("roomId"));
        String token = (String) payload.get("token");
        String messageType = (String) payload.get("messageType");
        System.out.println("messageType: " + messageType);
        System.out.println("payload: " + payload);
        if (messageType.equals("IMAGE")) {
            if (payload.containsKey("file") && payload.get("file") instanceof String) {
                String base64Image = (String) payload.get("file");
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                messageService.createMessage(roomId, messageType, token, "", imageBytes);
            }
        }
        else {
            String message = (String) payload.get("message");
            messageService.createMessage(roomId, messageType, token, message, null);
        }
        
        List<MessageResponse> messages = messageService.getRoomMessages(roomId, token);
        messagingTemplate.convertAndSend("/topic/" + roomId, messages);
   
    }

    @MessageMapping("/get-messages")
    public ResponseEntity<?> getMessages(@Payload Map<String, String> payload) throws BadRequestException, ForbiddenException {
        UUID roomId = UUID.fromString(payload.get("roomId"));
        String token = payload.get("token");
        List<MessageResponse> messages = messageService.getRoomMessages(roomId,token);
        messagingTemplate.convertAndSend ("/topic/"+roomId, messages);
        return ResponseEntity.ok(messages);
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
