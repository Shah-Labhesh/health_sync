package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.NotificationService;
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

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadNotificationsCount() throws BadRequestException, InternalServerErrorException {
        return notificationService.unreadNotificationsCount();
    }

    @GetMapping
    public ResponseEntity<?> getNotifications() throws BadRequestException, InternalServerErrorException {
        return notificationService.getNotifications();
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markRead() throws BadRequestException, InternalServerErrorException {
        return notificationService.markAsRead();
    }

}
