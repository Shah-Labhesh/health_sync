package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.*;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final ShareRecordRepo shareRecordRepo;
    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;
    private final RatingRepo ratingRepo;
    private final MedicalRecordRepo medicalRecordRepo;
    private final PrescriptionRepo prescriptionRepo;
    private final ChatRoomRepo chatRoomRepo;
    private final PaymentRepo paymentRepo;

    /*
    APPOINTMENT,
    PRESCRIPTION,
    MEDICAL_REPORT,
    CHAT,
    PAYMENT,
    ACCOUNT,
    REVIEW,
     */

    public ResponseEntity<?> unreadNotificationsCount() throws BadRequestException, InternalServerErrorException {
        try {
            String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(authentication);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("count", notificationRepo.countAllByUserAndRead(user, false));
            return ResponseEntity.ok(response);
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getNotifications() throws BadRequestException, InternalServerErrorException {
        try{String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(authentication);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            List<Notification> notifications =  notificationRepo.findAllByReceiver(user);

            notifications.forEach(notification -> notification.setReceiver(null));
            notifications.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
            return ResponseEntity.ok(notifications);}
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> markAsRead() throws BadRequestException, InternalServerErrorException {
       try{ String email = SecurityContextHolder.getContext().getAuthentication().getName();
           Users user = userRepo.findByEmail(email);
           if (user == null) {
               throw new BadRequestException("User not found");
           }
           for (Notification notification : notificationRepo.findAllByReceiverAndRead(user, false)
           ) {
               notification.setRead(true);
               notificationRepo.save(notification);
           }
           return ResponseEntity.created(null).body("Notification marked as read");}
       catch (BadRequestException e) {
           throw new BadRequestException(e.getMessage());
       }catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public void sendNotification(UUID id, String message, NotificationType notificationType, UUID receiverId) throws BadRequestException, InternalServerErrorException {
        try{Users receiver = userRepo.findById(receiverId).orElseThrow(() -> new BadRequestException("User not found"));
            Object target = null;
            switch (notificationType) {
                case APPOINTMENT -> {
                    target = appointmentRepo.findById(id).orElseThrow(() -> new BadRequestException("Appointment not found"));
                }
                case PRESCRIPTION -> {
                    target = prescriptionRepo.findById(id).orElseThrow(() -> new BadRequestException("Prescription not found"));
                }
                case MEDICAL_REPORT -> {
                    target = medicalRecordRepo.findById(id).orElseThrow(() -> new BadRequestException("Medical Report not found"));
                }
                case CHAT -> {
                    target = chatRoomRepo.findById(id).orElseThrow(() -> new BadRequestException("Chat Room not found"));
                }
                case PAYMENT -> {
                    target = paymentRepo.findById(id).orElseThrow(() -> new BadRequestException("Payment not found"));
                }
                case REVIEW -> {
                    target = ratingRepo.findById(id).orElseThrow(() -> new BadRequestException("Rating not found"));
                }
                case SHARE_RECORD -> {
                    target = shareRecordRepo.findById(id).orElseThrow(() -> new BadRequestException("Record not found"));
                }

            }

            notificationRepo.save(Notification.builder()
                    .targetId(id)
                    .type(notificationType)
                    .title(notificationType.name())
                    .receiver(receiver)
                    .createdAt(LocalDateTime.now())
                    .body(message)
                    .build());
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }


}
