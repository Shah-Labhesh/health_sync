package com.fyp.health_sync.service;


import com.fyp.health_sync.dtos.AddSlotDto;
import com.fyp.health_sync.dtos.UpdateSlotDto;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.SlotRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.SlotsResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepo slotRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> createSlot(AddSlotDto slot) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
            Users doctor = userRepo.findByEmail(currentPrincipalName);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to create slots");
            }

            if (!doctor.getApproved()){
                throw new BadRequestException("You are not approved yet");
            }


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            LocalDateTime dateTime = LocalDateTime.parse(slot.getSlotDateTime(), formatter);
            Integer existingSlots = slotRepo.findOverlappingSlots(doctor.getId(), dateTime, dateTime.plusMinutes(30));


            // query for cannot create slot if the new slot is within 1 hour of the existing slot : pending
            if (existingSlots > 0) {
                System.out.println("existingSlots = " + existingSlots);
                throw new BadRequestException("You already have a slot at this time");
            }


            Slots slots = Slots.builder()
                    .slotDateTime(dateTime)
                    .endTime(dateTime.plusMinutes(30))
                    .doctor(doctor)
                    .createdAt(LocalDateTime.now())
                    .isBooked(false)
                    .build();
            slotRepo.save(slots);

            return ResponseEntity.created(null).body(new SlotsResponse().castToResponse(slots));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }  catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateSlot(UUID slotId, UpdateSlotDto slot) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            Users doctor = userRepo.findByEmail(currentPrincipalName);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to update slots");
            }
            Slots slots = slotRepo.findById(slotId).orElseThrow(() -> new BadRequestException("Slot not found"));
            if (slots.getIsBooked()) {
                throw new BadRequestException("Slot is already booked");
            }
            if (slots.getDoctor().getId() != doctor.getId()) {
                throw new ForbiddenException("You are not authorized to update this slot");
            }

            if (slot.getSlotDateTime() != null) {
                Integer existingSlots = slotRepo.findOverlappingSlots(doctor.getId(), slot.getSlotDateTime(), slot.getSlotDateTime().plusMinutes(30));
                // query for cannot create slot if the new slot is within 1 hour of the existing slot : pending
                if (existingSlots > 0) {
                    System.out.println("existingSlots = " + existingSlots);
                    throw new BadRequestException("You already have a slot at this time");
                }
                slots.setSlotDateTime(slot.getSlotDateTime());
                slots.setEndTime(slot.getSlotDateTime().plusMinutes(30));
                slots.setUpdatedAt(LocalDateTime.now());
            }
            slotRepo.save(slots);
            return ResponseEntity.ok(new SlotsResponse().castToResponse(slots));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteSlot(UUID slotId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            Users doctor = userRepo.findByEmail(currentPrincipalName);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to delete slots");
            }
            Slots slots = slotRepo.findById(slotId).orElseThrow(() -> new BadRequestException("Slot not found"));

            if (slots.getIsBooked()) {
                throw new BadRequestException("Slot is already booked");
            }
            if (slots.getDoctor().getId() != doctor.getId()) {
                throw new ForbiddenException("You are not authorized to delete this slot");
            }
            slotRepo.deleteById(slotId);
            return ResponseEntity.ok("Slot deleted successfully");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getMySlots(String sort) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();
            Users doctor = userRepo.findByEmail(currentPrincipalName);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to get slots");
            }
            List<Slots> slots = slotRepo.findAllByDoctorIdOrderBySlotDateTime(doctor.getId());
            List<SlotsResponse> slotsResponses = new ArrayList<>();
            for (Slots slot : slots) {
                slotsResponses.add(new SlotsResponse().castToResponse(slot));
            }
            if (sort != null) {
                return switch (sort) {
                    case "ALL" -> ResponseEntity.ok(slotsResponses);
                    case "BOOKED" -> {
                        slotsResponses.removeIf(slot -> !slot.getIsBooked());
                        yield ResponseEntity.ok(slotsResponses);
                    }
                    case "UNBOOKED" -> {
                        slotsResponses.removeIf(SlotsResponse::getIsBooked);
                        yield ResponseEntity.ok(slotsResponses);
                    }
                    default -> throw new BadRequestException("Invalid sort parameter");
                };
            }
            return ResponseEntity.ok(slotsResponses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getDoctorSlots(UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to get slots");
            }
            List<Slots> slots = slotRepo.findByDoctorIdAndSlotDateTimeIsGreaterThan(doctorId, LocalDateTime.now());
            List<SlotsResponse> slotsResponses = new ArrayList<>();
            for (Slots slot : slots) {
                slotsResponses.add(new SlotsResponse().castToResponse(slot));
            }
            return ResponseEntity.ok(slotsResponses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
