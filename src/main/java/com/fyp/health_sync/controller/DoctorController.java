package com.fyp.health_sync.controller;

import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Get nearby doctors")
    @GetMapping("/nearby-doctors/{latitude}/{longitude}")
    public ResponseEntity<?> getNearbyDoctors(@PathVariable double latitude, @PathVariable double longitude) throws BadRequestException, InternalServerErrorException {
        return doctorService.getNearbyDoctors(latitude, longitude);
    }

    @Operation(summary = "toggle favorite")
    @PostMapping("/toggle-favorite/{doctorId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException {
        return doctorService.toggleFavorite(doctorId);
    }

    @Operation(summary = "get my favorites")
    @GetMapping("/my-favorites")
    public ResponseEntity<?> getMyFavorites() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return doctorService.getMyFavorites();
    }

    @Operation(summary = "Get doctor details by doctorId for users")
    @GetMapping("/doctor-details/{doctorId}")
    public ResponseEntity<?> getDoctorDetails(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException {
        return doctorService.getDoctorById(doctorId);
    }

    @Operation(summary = "Get doctor qualification by doctorId for users")
    @GetMapping("/qualification/{doctorId}")
    public ResponseEntity<?> getDoctorQualification(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException {
        return doctorService.getDoctorQualification(doctorId);
    }

    @Operation(summary = "Get doctor ratings by doctorId for users")
    @GetMapping("/ratings/{doctorId}")
    public ResponseEntity<?> getDoctorRatings(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException {
        return doctorService.getRatingsOfDoctor(doctorId);
    }

    @Operation(summary = "get my patients")
    @GetMapping("/my-patients")
    public ResponseEntity<?> getMyPatients() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return doctorService.getMyPatientList();
    }

    @Operation(summary = "get have appointments doctor")
    @GetMapping("/have-appointments")
    public ResponseEntity<?> getHaveAppointments() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return doctorService.getTalkedDoctorList();
    }

    @Operation(summary = "Filter doctor by speciality, fee, ratings")
    @GetMapping("/filter-doctors/{latitude}/{longitude}")
    public ResponseEntity<?> filterDoctors(@PathVariable double latitude, @PathVariable double longitude,
                                             @RequestParam(required = false) String searchText,
                                           @RequestParam(required = false) UUID speciality,
                                           @RequestParam(required = false) String feeType,
                                           @RequestParam(required = false) Integer feeFrom,
                                           @RequestParam(required = false) Integer feeTo,
                                           @RequestParam(required= false) Boolean popular,
                                           @RequestParam(required = false) Double ratings) throws BadRequestException, InternalServerErrorException {
        return doctorService.filterDoctors(latitude, longitude,searchText, speciality, feeType, feeFrom, feeTo, ratings, popular);
    }


}
