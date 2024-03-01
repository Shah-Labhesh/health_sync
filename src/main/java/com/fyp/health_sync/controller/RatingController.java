package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.RatingDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rating")
@SecurityRequirement(name = "BearerAuth")
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "Rate a doctor or user", description = "UserRole.USER, UserRole.DOCTOR", tags = {"Rating"})
    @PostMapping("/{id}")
    public ResponseEntity<?> rate(@PathVariable UUID id, @RequestBody @Valid RatingDto rating) throws BadRequestException, InternalServerErrorException {
        return ratingService.rate(id, rating);
    }
}
