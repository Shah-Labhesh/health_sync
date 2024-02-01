package com.fyp.health_sync.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.AddDoctorDetailsDto;
import com.fyp.health_sync.dtos.UpdateDoctorDto;
import com.fyp.health_sync.dtos.UploadAddressDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
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
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Get nearby doctors")
    @GetMapping("/nearby-doctors/{latitude}/{longitude}")
    public ResponseEntity<?> getNearbyDoctors(@PathVariable double latitude, @PathVariable double longitude) throws BadRequestException {
        return doctorService.getNearbyDoctors(latitude, longitude);
    }

    @Operation(summary = "toggle favorite")
    @PostMapping("/toggle-favorite/{doctorId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable UUID doctorId) throws BadRequestException {
        return doctorService.toggleFavorite(doctorId);
    }

    @Operation(summary = "get my favorites")
    @GetMapping("/my-favorites")
    public ResponseEntity<?> getMyFavorites() throws BadRequestException, ForbiddenException {
        return doctorService.getMyFavorites();
    }

    @Operation(summary = "Get doctor details by doctorId for users")
    @GetMapping("/doctor-details/{doctorId}")
    public ResponseEntity<?> getDoctorDetails(@PathVariable UUID doctorId) throws BadRequestException {
        return doctorService.getDoctorById(doctorId);
    }

    @Operation(summary = "Get doctor qualification by doctorId for users")
    @GetMapping("/qualification/{doctorId}")
    public ResponseEntity<?> getDoctorQualification(@PathVariable UUID doctorId ) throws BadRequestException {
        return doctorService.getDoctorQualification(doctorId);
    }

    @Operation(summary = "Get doctor ratings by doctorId for users")
    @GetMapping("/ratings/{doctorId}")
    public ResponseEntity<?> getDoctorRatings(@PathVariable UUID doctorId ) throws BadRequestException {
        return doctorService.getRatingsOfDoctor(doctorId);
    }

    @Operation(summary = "get my patients")
    @GetMapping("/my-patients")
    public ResponseEntity<?> getMyPatients() throws BadRequestException, ForbiddenException {
        return doctorService.getMyPatientList();
    }


    @Operation(summary = "get my appointments")
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments() throws BadRequestException, ForbiddenException {
        return doctorService.getMyAppointments();
    }


}
