package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.AddSlotDto;
import com.fyp.health_sync.dtos.UpdateSlotDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.service.SlotService;
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

    @PostMapping()
    public ResponseEntity<?> createSlot(@RequestBody @Valid AddSlotDto slot) throws BadRequestException {
        return slotService.createSlot(slot);
    }


    @PutMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(@PathVariable UUID slotId, @RequestBody @Valid UpdateSlotDto slot) throws BadRequestException, ForbiddenException {
        return slotService.updateSlot(slotId, slot);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable UUID slotId) throws BadRequestException, ForbiddenException {
        return slotService.deleteSlot(slotId);
    }


    @GetMapping("my-slots")
    public ResponseEntity<?> getMySlots() throws BadRequestException, ForbiddenException {
        return slotService.getMySlots();
    }

    @GetMapping("doctor-slots/{doctorId}")
    public ResponseEntity<?> getDoctorSlots(@PathVariable UUID doctorId) throws BadRequestException, ForbiddenException {
        return slotService.getDoctorSlots(doctorId);
    }

}
