package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get Unread Notifications Count", description = "UserRole.USER, UserRole.DOCTOR", tags = {"Notification"})
    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadNotificationsCount() throws BadRequestException, InternalServerErrorException {
        return notificationService.unreadNotificationsCount();
    }

    @Operation(summary = "Get Notifications", description = "UserRole.USER, UserRole.DOCTOR", tags = {"Notification"})
    @GetMapping
    public ResponseEntity<?> getNotifications() throws BadRequestException, InternalServerErrorException {
        return notificationService.getNotifications();
    }

    @Operation(summary = "Mark Notifications as Read", description = "UserRole.USER, UserRole.DOCTOR", tags = {"Notification"})
    @PostMapping("/mark-read")
    public ResponseEntity<?> markRead() throws BadRequestException, InternalServerErrorException {
        return notificationService.markAsRead();
    }

}
