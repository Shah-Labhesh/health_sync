package com.fyp.health_sync.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.*;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get Details of Logged User", description = "UserRole.All", tags = {"User"})
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/current-user")
    public ResponseEntity<?> getAllUser() throws BadRequestException, InternalServerErrorException {
        return userService.currentUser();
    }

    @Operation(summary = "Update Details of Logged User", description = "UserRole.All", tags = {"User"})
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/current-user")
    public ResponseEntity<?> updateUser(@RequestBody @Valid @ModelAttribute UpdateUserDto user) throws BadRequestException, InternalServerErrorException, IOException {
        return userService.updateUser(user);
    }


    @Operation(summary = "Upload address of doctor while registration", tags = {"User"} , description = "UserRole.DOCTOR")
    @PostMapping("/upload-address/{userId}")
    public ResponseEntity<?> uploadAddress(@PathVariable UUID userId, @RequestBody @Valid UploadAddressDto address) throws BadRequestException, JsonProcessingException, InternalServerErrorException, ForbiddenException {
        return userService.uploadAddress(userId, address);
    }


    @Operation(summary = "Upload details of doctor while registration", tags = {"User"} , description = "UserRole.DOCTOR")
    @PostMapping("/upload-details/{userId}")
    public ResponseEntity<?> uploadDetails(@PathVariable UUID userId, @RequestBody @Valid @ModelAttribute AddDoctorDetailsDto details) throws BadRequestException, IOException, InternalServerErrorException, ForbiddenException {
        return userService.uploadDetails(userId, details);
    }

    @Operation(summary = "Add khalti number of doctor", tags = {"User"} , description = "UserRole.DOCTOR")
    @PostMapping("/khaltiId/{doctorId}")
    public ResponseEntity<?> addQualification(@PathVariable UUID doctorId, @RequestBody @Valid AddMoreDetailsDto details) throws BadRequestException, IOException, InternalServerErrorException, ForbiddenException {
        return userService.saveKhalti(details, doctorId);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Store firebase token for push notification", tags = {"User"} , description = "UserRole.All")
    @PostMapping("/firebase-token")
    public ResponseEntity<?> saveFirebaseToken(@RequestBody @Valid String token) throws BadRequestException, InternalServerErrorException {
        return userService.saveFirebaseToken(token);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete firebase token while logout", tags = {"User"} , description = "UserRole.All")
    @DeleteMapping("/firebase-token")
    public ResponseEntity<?> deleteFirebaseToken(@RequestBody @Valid String token) throws BadRequestException, InternalServerErrorException {
        return userService.deleteFirebaseToken(token);
    }

    // request for Data Removal
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Request for data removal", tags = {"User"} , description = "UserRole.All")
    @PostMapping("/data-removal")
    public ResponseEntity<?> requestForDataRemoval(@RequestBody @Valid DataRemovalRequestDto request) throws BadRequestException, InternalServerErrorException {
        return userService.requestForDataRemoval(request);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get all data removal requests", tags = {"User"} , description = "UserRole.USER, UserRole.DOCTOR")
    @GetMapping("/data-removal")
    public ResponseEntity<?> getAllDataRemovalRequests() throws BadRequestException, InternalServerErrorException {
        return userService.getRequests();
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary="request for approval of account", tags = {"User"} , description = "UserRole.DOCTOR")
    @PostMapping("/approval")
    public ResponseEntity<?> requestForApproval() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return userService.requestForApproval();
    }

}
