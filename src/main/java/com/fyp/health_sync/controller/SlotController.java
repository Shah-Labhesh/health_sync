package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.AddSlotDto;
import com.fyp.health_sync.dtos.UpdateSlotDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/slots")
@SecurityRequirement(name = "BearerAuth")
public class SlotController {

    private final SlotService slotService;

    @Operation(summary = "Create a slot", description = "UserRole.DOCTOR", tags = {"Slot"})
    @PostMapping()
    public ResponseEntity<?> createSlot(@RequestBody @Valid AddSlotDto slot) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        return slotService.createSlot(slot);
    }

    @Operation(summary = "Update a slot", description = "UserRole.DOCTOR", tags = {"Slot"})
    @PutMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(@PathVariable UUID slotId, @RequestBody @Valid UpdateSlotDto slot) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return slotService.updateSlot(slotId, slot);
    }

    @Operation(summary = "Delete a slot", description = "UserRole.DOCTOR", tags = {"Slot"})
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable UUID slotId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return slotService.deleteSlot(slotId);
    }

    @Operation(summary = "Get all slots by self", description = "UserRole.DOCTOR", tags = {"Slot"})
    @GetMapping("my-slots")
    public ResponseEntity<?> getMySlots(@RequestParam(required = false) String sort) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return slotService.getMySlots(sort);
    }

    @Operation(summary = "Get all slots of doctor", description = "UserRole.USER", tags = {"Slot"})
    @GetMapping("doctor-slots/{doctorId}")
    public ResponseEntity<?> getDoctorSlots(@PathVariable UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        return slotService.getDoctorSlots(doctorId);
    }

}
