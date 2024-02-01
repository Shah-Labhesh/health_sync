package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.TakeAppointmentDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.PaymentStatus;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.AppointmentRepo;
import com.fyp.health_sync.repository.SlotRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.AppointmentResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final UserRepo userRepo;
    private final SlotRepo slotRepo;


    public ResponseEntity<?> createAppointment(TakeAppointmentDto takeAppointmentDto) throws BadRequestException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();

            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }

            Users doctors = userRepo.findById(takeAppointmentDto.getDoctorId()).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctors.getRole() != UserRole.DOCTOR) {
                throw new BadRequestException("Doctor Id is not valid");
            }
            if (doctors.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Doctor not found");
            }

            Slots slots = slotRepo.findById(takeAppointmentDto.getSlotId()).orElseThrow(() -> new BadRequestException("Slot not found"));
            if (slots.getIsBooked()) {
                throw new BadRequestException("Slot already booked");
            }

            Appointments appointments = Appointments.builder()
                    .slot(slots)
                    .createdAt(LocalDateTime.now())
                    .notes(takeAppointmentDto.getNotes())
                    .doctor(doctors)
                    .appointmentType(takeAppointmentDto.getAppointmentType())
                    .user(users)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
            appointmentRepo.save(appointments);
            slots.setIsBooked(true);
            slotRepo.save(slots);
            return ResponseEntity.ok(new SuccessResponse("Appointment booked successfully"));

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
            System.out.println(users.getId());
            List<AppointmentResponse> list = new ArrayList<>();
            if (users.getRole() == UserRole.DOCTOR){
                for (Appointments appointments : appointmentRepo.findAllByDoctor(users)) {
                    list.add(new AppointmentResponse().castToResponse(appointments));

                }
                return ResponseEntity.ok(list);
            }
            for (Appointments appointments : appointmentRepo.findAllByUser(users)) {
                list.add(new AppointmentResponse().castToResponse(appointments));

            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
