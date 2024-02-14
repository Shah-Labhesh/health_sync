package com.fyp.health_sync.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.AddDoctorDetailsDto;
import com.fyp.health_sync.dtos.AddMoreDetailsDto;
import com.fyp.health_sync.dtos.UpdateUserDto;
import com.fyp.health_sync.dtos.UploadAddressDto;
import com.fyp.health_sync.exception.BadRequestException;
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

    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/current-user")
    public ResponseEntity<?> getAllUser() throws BadRequestException, InternalServerErrorException {
        return userService.currentUser();
    }

    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/current-user")
    public ResponseEntity<?> updateUser(@RequestBody @Valid @ModelAttribute UpdateUserDto user) throws BadRequestException, InternalServerErrorException, IOException {
        return userService.updateUser(user);
    }

    @Operation(summary = "Upload address of doctor")
    @PostMapping("/upload-address/{userId}")
    public ResponseEntity<?> uploadAddress(@PathVariable UUID userId, @RequestBody @Valid UploadAddressDto address) throws BadRequestException, JsonProcessingException, InternalServerErrorException {
        return userService.uploadAddress(userId, address);
    }

    @Operation(summary = "Upload details of doctor")
    @PostMapping("/upload-details/{userId}")
    public ResponseEntity<?> uploadDetails(@PathVariable UUID userId, @RequestBody @Valid @ModelAttribute AddDoctorDetailsDto details) throws BadRequestException, IOException, InternalServerErrorException {
        return userService.uploadDetails(userId, details);
    }

    @Operation(summary = "Add khalti number of doctor")
    @PostMapping("/khaltiId/{doctorId}")
    public ResponseEntity<?> addQualification(@PathVariable UUID doctorId, @RequestBody @Valid AddMoreDetailsDto details) throws BadRequestException, IOException, InternalServerErrorException {
        return userService.saveKhalti(details, doctorId);
    }

    @PostMapping("/firebase-token")
    public ResponseEntity<?> saveFirebaseToken(@RequestBody @Valid String token) throws BadRequestException, InternalServerErrorException {
        return userService.saveFirebaseToken(token);
    }

    @DeleteMapping("/firebase-token")
    public ResponseEntity<?> deleteFirebaseToken(@RequestBody @Valid String token) throws BadRequestException, InternalServerErrorException {
        return userService.deleteFirebaseToken(token);
    }

}
