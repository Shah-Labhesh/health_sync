package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.SpecialityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/speciality")
@RequiredArgsConstructor
public class SpecialityController {

    private final SpecialityService specialityService;

    @Operation(summary = "Get all specialities")
    @GetMapping("/all")
    public ResponseEntity<?> getAllSpecialities() throws InternalServerErrorException {
        return specialityService.getAllSpecialities();
    }
}
