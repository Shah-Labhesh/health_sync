package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.UploadPrescriptionDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fyp.health_sync.service.PrescriptionService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prescription")
@SecurityRequirement(name = "BearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "Upload a prescription by the user",description = "UserRole.DOCTOR",tags = {"Prescription"})
    @PostMapping
    public ResponseEntity<?> savePrescription(@ModelAttribute @RequestBody @Valid UploadPrescriptionDto prescription) throws ForbiddenException, BadRequestException, InternalServerErrorException, IOException {
        return prescriptionService.savePrescription(prescription);
    }

    @Operation(summary = "Get all prescriptions added",description = "UserRole.DOCTOR, UserRole.USER", tags = {"Prescription"})
    @GetMapping
    public ResponseEntity<?> getPrescriptions() throws BadRequestException, InternalServerErrorException {
        return prescriptionService.getMyPrescriptions();
    }

    @Operation(summary = "Get all prescriptions of user",description = "UserRole.DOCTOR", tags = {"Prescription"})
    @GetMapping("/{userId}")
    public ResponseEntity<?> getPrescriptionsOfUser(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.getPrescriptionsOfUser(userId);
    }

    @Operation(summary = "Request permission to view prescription",description = "UserRole.DOCTOR", tags = {"Prescription"})
    @PostMapping("/permission/{userId}")
    public ResponseEntity<?> requestPermission(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.requestPrescription(userId);
    }

    @Operation(summary = "Accept or reject permission requests",description = "UserRole.USER", tags = {"Prescription"})
    @PostMapping("/permission/{PermissionId}/{value}")
    public ResponseEntity<?> acceptRejectPermission(@PathVariable UUID PermissionId, @PathVariable boolean value) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.acceptOrRejectRequest(PermissionId, value);
    }

    @Operation(summary = "Get all permissions requested",description = "UserRole.USER, UserRole.Doctor", tags = {"Prescription"})
    @GetMapping("/permission")
    public ResponseEntity<?> getPermissionRequests() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.getAllRequestPermission();
    }

    @Operation(summary = "Revoking permission to view prescription",description = "UserRole.USER", tags = {"Prescription"})
    @DeleteMapping("/permission/{PermissionId}")
    public ResponseEntity<?> revokePermission(@PathVariable UUID PermissionId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.revokePermission(PermissionId);
    }

    @Operation(summary = "Cancel permission request",description = "UserRole.DOCTOR", tags = {"Prescription"})
    @DeleteMapping("/permission/cancel/{PermissionId}")
    public ResponseEntity<?> cancelPermissionRequest(@PathVariable UUID PermissionId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return prescriptionService.cancelPermissionRequest(PermissionId);
    }


}
