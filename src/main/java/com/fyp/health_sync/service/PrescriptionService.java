package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.UploadPrescriptionDto;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Prescriptions;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.PrescriptionRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ImageUtils;
import com.fyp.health_sync.utils.PrescriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final FirebaseTokenRepo firebaseTokenRepo;

    public ResponseEntity<?> savePrescription(UploadPrescriptionDto prescription) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users doctor = userRepo.findByEmail(email);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ForbiddenException("You are not authorized to upload prescription");
        }
        Users user = userRepo.findById(prescription.getUserId()).orElseThrow(() -> new BadRequestException("User not found"));
        if (user.getRole() != UserRole.USER) {
            throw new BadRequestException("User is not a patient");
        }
        if (prescription.getRecordType().equals(RecordType.IMAGE.name()) || prescription.getRecordType().equals(RecordType.PDF.name())) {
            if (prescription.getPrescription() == null) {
                throw new BadRequestException("Prescription must not be empty");
            }
        } else if (prescription.getRecordType().equals(RecordType.TEXT.name())) {
            if (prescription.getPrescriptionText() == null) {
                throw new BadRequestException("Prescription text must not be empty");
            }
        }
            Prescriptions prescriptions = prescriptionRepo.save(Prescriptions.builder()
                    .recordType(prescription.getRecordType())
                    .prescription(prescription.getPrescription() != null ? ImageUtils.compress(prescription.getPrescription().getBytes()) : null)
                    .prescriptionText(prescription.getPrescriptionText())
                    .createdAt(LocalDateTime.now())
                    .doctor(doctor)
                    .user(user)
                    .build()
            );
            notificationService.sendNotification(prescriptions.getId(), "You have a new prescription from Dr. " + prescriptions.getDoctor().getName(), NotificationType.PRESCRIPTION, prescriptions.getUser().getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(user)) {
                pushNotificationService.sendNotification("New Prescription", "You have a new prescription from Dr. " + prescriptions.getDoctor().getName(), token.getToken());
            }
            return ResponseEntity.created(null).body(new PrescriptionResponse().castToResponse(prescriptions));
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getMyPrescriptions() throws BadRequestException, InternalServerErrorException {
        try {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        List<PrescriptionResponse> prescriptionResponses = new ArrayList<>();
            if (user.getRole() == UserRole.DOCTOR) {
                for (Prescriptions prescription : prescriptionRepo.findByDoctor(user)) {
                    prescriptionResponses.add(new PrescriptionResponse().castToResponse(prescription));
                }
            } else {
                for (Prescriptions prescription : prescriptionRepo.findByUser(user)) {
                    prescriptionResponses.add(new PrescriptionResponse().castToResponse(prescription));
                }
            }
            return ResponseEntity.ok(prescriptionResponses);
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
