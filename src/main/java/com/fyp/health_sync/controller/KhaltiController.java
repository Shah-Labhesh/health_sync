package com.fyp.health_sync.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.ConfirmRequestDto;
import com.fyp.health_sync.dtos.KhaltiRequestDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.KhaltiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khalti")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class KhaltiController {

    private final KhaltiService khaltiService;

    @Operation(summary = "Initiate Transaction", description = "UserRole.USER", tags = {"Khalti"})
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTransaction(@RequestBody @Valid KhaltiRequestDto khaltiRequest) throws  ForbiddenException, BadRequestException, InternalServerErrorException {
        return khaltiService.initiateTransaction(khaltiRequest);
    }

    @Operation(summary = "Confirm Transaction", description = "UserRole.USER", tags = {"Khalti"})
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody @Valid ConfirmRequestDto khaltiRequest) throws  ForbiddenException, BadRequestException, InternalServerErrorException {
        return khaltiService.confirmTransaction(khaltiRequest);
    }


}
