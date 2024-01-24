package com.fyp.health_sync.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.AddDoctorDetailsDto;
import com.fyp.health_sync.dtos.UpdateDoctorDto;
import com.fyp.health_sync.dtos.UploadAddressDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/current-doctor")
    public ResponseEntity<?> getCurrentDoctor() throws BadRequestException {

        return doctorService.currentDoctor();
    }

    @Operation(summary = "Upload address of doctor")
    @PostMapping("/upload-address/{doctorId}")
    public ResponseEntity<?> uploadAddress(@PathVariable UUID doctorId, @RequestBody @Valid UploadAddressDto address) throws BadRequestException, JsonProcessingException {
    return doctorService.uploadAddress(doctorId, address);
    }

    @Operation(summary = "Upload details of doctor")
    @PostMapping("/upload-details/{doctorId}")
    public ResponseEntity<?> uploadDetails(@PathVariable UUID doctorId, @RequestBody @Valid @ModelAttribute AddDoctorDetailsDto details) throws BadRequestException, IOException {
        return doctorService.uploadDetails(doctorId, details);
    }

    @Operation(summary = "Get doctor details by doctorId for users")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/doctor-details/{doctorId}")
    public ResponseEntity<?> getDoctorDetails(@PathVariable UUID doctorId) throws BadRequestException {
        return doctorService.getDoctorDetails(doctorId);
    }

    @Operation(summary = "Update Doctor Details with authentication")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/current-doctor")
    public ResponseEntity<?> updateAddress(@RequestBody @Valid UpdateDoctorDto doctor) throws BadRequestException {
        return doctorService.updateDoctorDetails(doctor);
    }




}
