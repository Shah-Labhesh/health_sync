package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.UploadPrescriptionDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fyp.health_sync.service.PrescriptionService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prescription")
@SecurityRequirement(name = "BearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<?> savePrescription(@ModelAttribute @RequestBody @Valid UploadPrescriptionDto prescription) throws ForbiddenException, BadRequestException, InternalServerErrorException, IOException {
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping
    public ResponseEntity<?> getPrescriptions() throws BadRequestException, InternalServerErrorException {
        return prescriptionService.getMyPrescriptions();
    }
}
