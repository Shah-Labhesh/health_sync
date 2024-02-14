package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.TakeAppointmentDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.PaymentStatus;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.AppointmentRepo;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.SlotRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.AppointmentResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final UserRepo userRepo;
    private final SlotRepo slotRepo;
    private final FirebaseTokenRepo firebaseTokenRepo;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;


    private String getAppointmentId() {
        // generate 5-6 digit random number
        int id = (int) (Math.random() * 900000) + 100000;
        return "AP" + id;
    }

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
                    .reminderTime(slots.getSlotDateTime().minusMinutes(takeAppointmentDto.getReminderTime()))
                    .appointmentType(takeAppointmentDto.getAppointmentType())
                    .user(users)
                    .isExpired(false)
                    .paymentStatus(PaymentStatus.PENDING)
                    .appointmentFee(takeAppointmentDto.getAppointmentFee())
                    .platformCost(takeAppointmentDto.getPlatformCost())
                    .totalFee(takeAppointmentDto.getTotalFee())
                    .build();
            appointmentRepo.save(appointments);
            slots.setIsBooked(true);
            slotRepo.save(slots);
            notificationService.sendNotification(appointments.getId(), "You have a new appointment with " + appointments.getUser().getName(), NotificationType.APPOINTMENT, doctors.getId());
            notificationService.sendNotification(appointments.getId(), "Your Appointment with Dr. " + appointments.getDoctor().getName() + " is booked.", NotificationType.APPOINTMENT, users.getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(users)) {
                pushNotificationService.sendNotification("Appointment Booked", "Your Appointment with Dr. " + appointments.getDoctor().getName() + " is booked.", token.getToken());
            }
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(doctors)) {
                pushNotificationService.sendNotification("New Appointment", "You have a new appointment with " + appointments.getUser().getName(), token.getToken());
            }
            return ResponseEntity.created(null).body(new SuccessResponse("Appointment booked successfully"));

        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getAppointment() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();
            if (userRepo.findByEmail(user) == null) {
                throw new BadRequestException("User not found");
            }
            if (userRepo.findByEmail(user).getRole() == UserRole.DOCTOR) {
                return getDoctorAppointments();
            } else {
                return getUserAppointment();
            }
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    private ResponseEntity<?> getUserAppointment() throws BadRequestException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();

            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            System.out.println(users.getId());
            List<AppointmentResponse> list = new ArrayList<>();
            if (users.getRole() == UserRole.DOCTOR) {
                for (Appointments appointments : appointmentRepo.findAllByDoctor(users)) {
                    list.add(new AppointmentResponse().castToResponse(appointments));
                }
            } else {
                for (Appointments appointments : appointmentRepo.findAllByUser(users)) {
                    list.add(new AppointmentResponse().castToResponse(appointments));
                }
            }
            list.removeIf(appointment -> appointment.getSlot().getSlotDateTime().isBefore(LocalDateTime.now()));
            list.sort(Comparator.comparing(o -> o.getSlot().getSlotDateTime()));
            // order by slot date time

            return ResponseEntity.ok(list);
        }
        catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // get my appointment list from appointments order by slotDateTime
    private ResponseEntity<?> getDoctorAppointments() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users doctor = userRepo.findByEmail(authentication.getName());
            if (doctor == null) {
                throw new BadRequestException("User not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to get patient list");
            }
            List<Appointments> appointments = appointmentRepo.findAllByDoctor(doctor);
            List<AppointmentResponse> responses = new ArrayList<>();
            for (Appointments appointment : appointments) {
                responses.add(new AppointmentResponse().castToResponse(appointment));
            }

            responses.removeIf(appointmentResponse -> appointmentResponse.getSlot().getSlotDateTime().isBefore(LocalDateTime.now()));

            responses.sort(Comparator.comparing(o -> o.getSlot().getSlotDateTime()));
            return ResponseEntity.ok(responses);
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


}
