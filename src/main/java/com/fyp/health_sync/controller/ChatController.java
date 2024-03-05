package com.fyp.health_sync.controller;

import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @Operation(summary = "Get my chat rooms", description = "UserRole.USER, UserRole.Doctor", tags = {"Chat"})
    @GetMapping("/my-rooms")
    public ResponseEntity<?> getChatRoom() throws BadRequestException, InternalServerErrorException {
        return messageService.getMyChatRooms();
    }

    @Operation(summary = "Create chat room", description = "UserRole.USER, UserRole.Doctor", tags = {"Chat"})
    @PostMapping("/room")
    public ResponseEntity<?> createChatRoom(@RequestParam UUID doctorId) throws BadRequestException {
        return messageService.createChatRoom(doctorId);
    }
}
