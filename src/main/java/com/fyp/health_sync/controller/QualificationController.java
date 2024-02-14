package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.AddMoreDetailsDto;
import com.fyp.health_sync.dtos.QualificationDto;
import com.fyp.health_sync.dtos.UpdateQualificationDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/qualification")
public class QualificationController {

    private final QualificationService qualificationService;

    @Operation(summary = "Add qualification of doctor")
    @PostMapping("/{doctorId}")
    public ResponseEntity<?> addQualification(@PathVariable UUID doctorId,@ModelAttribute @RequestBody @Valid QualificationDto qualification) throws BadRequestException, IOException, InternalServerErrorException {
        return qualificationService.addQualification(qualification, doctorId);
    }

   @Operation(summary = "Update qualification of doctor")
   @PutMapping("/{doctorId}/{qualificationId}")
   public ResponseEntity<?> updateQualification(@PathVariable UUID doctorId,@ModelAttribute @PathVariable UUID qualificationId,@ModelAttribute @RequestBody @Valid UpdateQualificationDto qualification) throws BadRequestException, ForbiddenException, IOException, InternalServerErrorException {
       return qualificationService.updateQualificationByDoctorId(doctorId,qualificationId, qualification);
   }

   @Operation(summary = "Delete qualification of doctor")
   @DeleteMapping("/{doctorId}/{qualificationId}")
   public ResponseEntity<?> deleteQualification(@PathVariable UUID doctorId, @PathVariable UUID qualificationId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
       return qualificationService.deleteQualificationByDoctorId(doctorId, qualificationId);
   }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Add qualification of doctor after authentication")
    @PostMapping("/auth")
    public ResponseEntity<?> addQualificationAuth(@ModelAttribute @RequestBody @Valid QualificationDto qualification) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return qualificationService.saveQualificationAuth(qualification);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get qualification of doctor by users")
    @GetMapping("/user/{doctorId}")
    public ResponseEntity<?> getQualification(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return qualificationService.getQualificationById( doctorId);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "update qualification of doctor after authentication")
    @PutMapping("/auth/{qualificationId}")
    public ResponseEntity<?> updateQualification(@PathVariable UUID qualificationId,@ModelAttribute @RequestBody @Valid UpdateQualificationDto qualification) throws BadRequestException, ForbiddenException, IOException, InternalServerErrorException {
        return qualificationService.updateQualification(qualificationId, qualification);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "delete qualification of doctor after authentication")
    @DeleteMapping("/auth/{qualificationId}")
    public ResponseEntity<?> deleteQualification(@PathVariable UUID qualificationId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return qualificationService.deleteQualification(qualificationId);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get qualification of doctor himself")
    @GetMapping("/my-qualification")
    public ResponseEntity<?> getMyQualification() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return qualificationService.getMyQualification();
    }

    @Operation(summary = "Get qualification of doctor himself")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getQualificationOfDoctor(@PathVariable UUID doctorId) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return qualificationService.getQualificationById( doctorId);
    }
}
