package com.fyp.health_sync.controller;

import com.fyp.health_sync.dtos.AddMedicalRecordDto;
import com.fyp.health_sync.dtos.UpdateMedicalRecordDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@RequestMapping("/api/v1/medical-record")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Operation(summary = "Add Medical Record by user")
    @PostMapping
    public ResponseEntity<?> addMedicalRecord(@ModelAttribute @RequestBody @Valid AddMedicalRecordDto recordDto) throws BadRequestException, IOException, InternalServerErrorException {
        return medicalRecordService.uploadRecord(recordDto);
    }

    @Operation(summary = "upload Medical Record by Doctor")
    @PostMapping("/{userId}")
    public ResponseEntity<?> uploadMedicalRecord(@ModelAttribute @RequestBody @Valid AddMedicalRecordDto recordDto, @PathVariable UUID userId) throws BadRequestException, IOException, InternalServerErrorException {
        return medicalRecordService.uploadRecordByDoctor(recordDto, userId);
    }


    @Operation(summary = "Get all Medical Record of user")
    @GetMapping
    public ResponseEntity<?> getAllMedicalRecordOfUser() throws BadRequestException {
        return medicalRecordService.getAllRecord();
    }

    @Operation(summary = "Get Medical Record by user")
    @GetMapping("/{recordId}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable UUID recordId) throws BadRequestException, ForbiddenException {
        return  medicalRecordService.getRecordById(recordId);
    }

    @Operation(summary = "Update Medical Record by user")
    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateMedicalRecord(@PathVariable UUID recordId, @RequestBody @Valid UpdateMedicalRecordDto recordDto) throws BadRequestException, ForbiddenException {
        return medicalRecordService.updateMedicalRecord(recordDto, recordId );
    }

    @Operation(summary = "Delete Medical Record by user")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteMedicalRecordById( @PathVariable UUID recordId) throws BadRequestException, ForbiddenException {
        return medicalRecordService.deleteMedicalRecord(recordId);
    }

    // share medical record to doctor
    @Operation(summary = "Share Medical Record by user")
    @PostMapping("/share/{recordId}/{doctorId}")
    public ResponseEntity<?> shareMedicalRecord(@PathVariable UUID recordId, @PathVariable UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.shareMedicalRecord(recordId, doctorId);
    }
}
