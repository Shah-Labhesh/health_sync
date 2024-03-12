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

    @Operation(summary = "Add Medical Record",description = "UserRole.USER",tags = {"Medical Record"})
    @PostMapping
    public ResponseEntity<?> addMedicalRecord(@ModelAttribute @RequestBody @Valid AddMedicalRecordDto recordDto) throws BadRequestException, IOException, InternalServerErrorException {
        return medicalRecordService.uploadRecord(recordDto);
    }

    @Operation(summary = "upload Medical Record", description = "UserRole.DOCTOR", tags = {"Medical Record"})
    @PostMapping("/{userId}")
    public ResponseEntity<?> uploadMedicalRecord(@ModelAttribute @RequestBody @Valid AddMedicalRecordDto recordDto, @PathVariable UUID userId) throws BadRequestException, IOException, InternalServerErrorException {
        return medicalRecordService.uploadRecordByDoctor(recordDto, userId);
    }


    @Operation(summary = "Get all Medical Record by self",description = "UserRole.USER",tags = {"Medical Record"})
    @GetMapping
    public ResponseEntity<?> getAllMedicalRecordOfUser(@RequestParam(required = true,defaultValue = "ALL") String sort) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.getAllRecordByUser(sort);
    }

    @Operation(summary = "Get all Medical Record of user", description = "UserRole.DOCTOR", tags = {"Medical Record"})
    @GetMapping("/view/{userId}")
    public ResponseEntity<?> getAllMedicalRecordOfUserByDoctor(@PathVariable UUID userId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.getAllRecordOfUser(userId);
    }

    @Operation(summary = "Get Medical Record by id", description = "UserRole.USER", tags = {"Medical Record"})
    @GetMapping("/{recordId}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return  medicalRecordService.getRecordById(recordId);
    }

    @Operation(summary = "Update Medical Record by id", description = "UserRole.USER", tags = {"Medical Record"})
    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateMedicalRecord(@PathVariable UUID recordId,@ModelAttribute @RequestBody @Valid UpdateMedicalRecordDto recordDto) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.updateMedicalRecord(recordDto, recordId );
    }

//    @Operation(summary = "Delete Medical Record by user")
//    @DeleteMapping("/{recordId}")
//    public ResponseEntity<?> deleteMedicalRecordById( @PathVariable UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
//        return medicalRecordService.deleteMedicalRecord(recordId);
//    }

    @Operation(summary = "accept or reject permission to view reocrds", description = "UserRole.USER", tags = {"Medical Record"})
    @PostMapping("/approval/{requestId}/{value}")
    public ResponseEntity<?> acceptOrRejectRecord( @PathVariable UUID requestId, @PathVariable boolean value) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.acceptOrRejectRecord(requestId,value);
    }

    @Operation(summary = "request for viewing medical record", description = "UserRole.DOCTOR", tags = {"Medical Record"})
    @PostMapping("/request/{userId}")
    public ResponseEntity<?> requestForViewingMedicalRecord(@PathVariable UUID userId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.requestForViewingRecord(userId);
    }

    @Operation(summary = "Get all request for viewing medical record", description = "UserRole.USER", tags = {"Medical Record"})
    @GetMapping("/request")
    public ResponseEntity<?> getAllRequestForViewingMedicalRecord() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.getAllRequestForViewingRecord();
    }

    @Operation(summary = "cancel request for viewing medical record", description = "UserRole.DOCTOR", tags = {"Medical Record"})
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<?> cancelRequestForViewingMedicalRecord(@PathVariable UUID requestId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return medicalRecordService.cancelRequest(requestId);
    }



}
