package com.fyp.health_sync.service;


import com.fyp.health_sync.dtos.AddSlotDto;
import com.fyp.health_sync.dtos.UpdateSlotDto;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.SlotRepo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepo slotRepo;
    private final DoctorRepo doctorRepo;

    public ResponseEntity<?> createSlot(AddSlotDto slot) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(slot.getSlotDateTime().toString(), formatter);
        // if doctor has same slot
        Slots slots1 = slotRepo.findBySlotDateTimeAndDoctor(dateTime, doctor);
        Slots slots2 = slotRepo.findBySlotDateTimeAndDoctor(dateTime.plusHours(1), doctor);

        if (slots1 != null || slots2 != null){
            throw new BadRequestException("You already have a slot at this time");
        }
        // same doctor should have 1 hour gap between slots


        Slots slots = Slots.builder()
                .slotDateTime(dateTime)
                .doctor(doctor)
                .createdAt(LocalDateTime.now())
                .isBooked(false)
                .build();
        return ResponseEntity.created(null) .body(slotRepo.save(slots));
    }

    public ResponseEntity<?> updateSlot(UUID slotId, UpdateSlotDto slot) throws BadRequestException, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
       Slots slots = slotRepo.findBySlotId(slotId);
        if (slots == null) {
            throw new BadRequestException("Slot not found");
        }
        if (slots.getIsBooked()){
            throw new BadRequestException("Slot is already booked");
        }
        if (slots.getDoctor().getId() != doctor.getId()){
            throw new ForbiddenException("You are not authorized to update this slot");
        }
        if (slot.getSlotDateTime() != null){
            slots.setSlotDateTime(slot.getSlotDateTime());
            slots.setUpdatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(slotRepo.save(slots));
    }

    @Transactional
    public ResponseEntity<?> deleteSlot(UUID slotId) throws BadRequestException, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        Slots slots = slotRepo.findBySlotId(slotId);
        if (slots == null) {
            throw new BadRequestException("Slot not found");
        }
        if (slots.getIsBooked()){
            throw new BadRequestException("Slot is already booked");
        }
        if (slots.getDoctor().getId() != doctor.getId()){
            throw new ForbiddenException("You are not authorized to delete this slot");
        }
        slotRepo.deleteById(slotId);
        return ResponseEntity.ok("Slot deleted successfully");
    }

    public ResponseEntity<?> getMySlots() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        
        return ResponseEntity.ok(slotRepo.findAllByDoctorIdOrderBySlotDateTime(doctor.getId()));
    }

    public ResponseEntity<?> getDoctorSlots(UUID doctorId) throws BadRequestException {
        Optional<Doctors> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()) {
            throw new BadRequestException("Doctor not found");
        }
        return ResponseEntity.ok(slotRepo.findByDoctorIdAndIsBookedIsFalseAndSlotDateTimeIsGreaterThanEqual(doctorId, LocalDateTime.now()));
    }
}
