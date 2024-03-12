package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.ContactSupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@RestController
@SecurityRequirement(name = "BearerAuth")
public class ContactController {

    private final ContactSupportService contactSupportService;


    @Operation(summary = "Contact Support", description = "UserRole.USER, UserRole.DOCTOR", tags = {"Contact Us"})
    @PostMapping
    public ResponseEntity<?> contactSupport(String email, String message) throws BadRequestException, InternalServerErrorException {
        return contactSupportService.contactSupport(email, message);
    }

    @Operation(summary = "Get all messages", description = "UserRole.ADMIN", tags = {"Contact Us"})
    @GetMapping
    public ResponseEntity<?> getAllMessages() throws InternalServerErrorException, BadRequestException, ForbiddenException {
        return contactSupportService.getAllMessages();
    }

    @Operation(summary = "Send Response of message to user", description = "UserRole.ADMIN", tags = {"Contact Us"})
    @PostMapping("/response/{messageId}")
    public ResponseEntity<?> responseMessage(@RequestParam(required = false) String responseMessage,@PathVariable UUID messageId) throws InternalServerErrorException, BadRequestException, ForbiddenException {
        return contactSupportService.responseMessage(responseMessage, messageId);
    }
}
