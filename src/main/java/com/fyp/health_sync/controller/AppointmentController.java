package com.fyp.health_sync.controller;

import com.fyp.health_sync.dtos.TakeAppointmentDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.AppointmentService;
import com.fyp.health_sync.service.PushNotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;


    @Operation(summary = "Take an appointment",description = "UserRole.USER",tags = {"Appointment"})
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody @Valid TakeAppointmentDto takeAppointmentDto) throws BadRequestException, InternalServerErrorException {

        return appointmentService.createAppointment(takeAppointmentDto);
    }

//    @Operation(summary = "Cancel an appointment",description = "UserRole.USER",tags = {"appointment"})
    @Operation(summary = "Get your appointment from today",description = "UserRole.USER, UerRole.DOCTOR",tags = {"Appointment"})
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return appointmentService.getAppointment();
    }

    @Operation(summary = "Get All your appointments",description = "UserRole.USER, UerRole.DOCTOR",tags = {"Appointment"})
    @GetMapping("/all-appointments")
    public ResponseEntity<?> getAllAppointments() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return appointmentService.getAllMyAppointment();
    }


}
