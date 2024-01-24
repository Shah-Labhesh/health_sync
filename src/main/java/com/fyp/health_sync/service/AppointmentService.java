package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.TakeAppointmentDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.PaymentStatus;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.AppointmentRepo;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.SlotRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;
    private final SlotRepo slotRepo;


    public ResponseEntity<?> createAppointment(TakeAppointmentDto takeAppointmentDto) throws BadRequestException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();

            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }

            Doctors doctors = doctorRepo.findById(takeAppointmentDto.getDoctorId()).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctors.getAccountStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Doctor not found");
            }

            Slots slots = slotRepo.findById(takeAppointmentDto.getSlotId()).orElseThrow(() -> new BadRequestException("Slot not found"));
            if (slots.getIsBooked()) {
                throw new BadRequestException("Slot already booked");
            }

            Appointments appointments = Appointments.builder()
                    .slotId(slots)
                    .createdAt(LocalDateTime.now())
                    .notes(takeAppointmentDto.getNotes())
                    .doctorId(doctors)
                    .appointmentType(takeAppointmentDto.getAppointmentType())
                    .UserId(users)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
            appointmentRepo.save(appointments);
            slots.setIsBooked(true);
            slotRepo.save(slots);
            return ResponseEntity.ok(new SuccessResponse("Appointment created successfully"));

        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getAppointment() throws BadRequestException, InternalServerErrorException {
        try{
            String user = SecurityContextHolder.getContext().getAuthentication().getName();

            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            return ResponseEntity.ok(appointmentRepo.findAllByUserId(users));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
