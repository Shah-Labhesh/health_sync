package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.AddMedicalRecordDto;
import com.fyp.health_sync.dtos.UpdateMedicalRecordDto;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.ShareMedicalRecords;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.MedicalRecordRepo;
import com.fyp.health_sync.repository.ShareRecordRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ImageUtils;
import com.fyp.health_sync.utils.RecordResponse;
import com.fyp.health_sync.utils.SharedRecordResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepo medicalRecordRepo;
    private final UserRepo userRepo;
    private final ShareRecordRepo shareRecordRepo;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final FirebaseTokenRepo firebaseTokenRepo;

    public ResponseEntity<?> uploadRecord(AddMedicalRecordDto recordDto) throws BadRequestException,  InternalServerErrorException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            MedicalRecords medicalRecords = MedicalRecords.builder()
                    .recordType(recordDto.getRecordType())
                    .record(ImageUtils.compress(recordDto.getRecord().getBytes()))
                    .medicalRecordType(recordDto.getMedicalRecordType())
                    .selfAdded(true)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            medicalRecordRepo.save(medicalRecords);
            return ResponseEntity.created(null).body(new RecordResponse().castToResponse(medicalRecords));
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> uploadRecordByDoctor(AddMedicalRecordDto recordDto, UUID userId) throws BadRequestException, IOException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BadRequestException("You are not authorized to add record");
            }
            Users patient = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
            MedicalRecords medicalRecords = MedicalRecords.builder()
                    .recordType(recordDto.getRecordType())
                    .record(ImageUtils.compress(recordDto.getRecord().getBytes()))
                    .medicalRecordType(recordDto.getMedicalRecordType())
                    .selfAdded(false)
                    .user(patient)
                    .doctor(doctor)
                    .createdAt(LocalDateTime.now())
                    .build();
            medicalRecordRepo.save(medicalRecords);

            notificationService.sendNotification(medicalRecords.getId(),"You have a new medical record from Dr. "+medicalRecords.getDoctor().getName(),NotificationType.MEDICAL_REPORT, medicalRecords.getUser().getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(patient)) {
                pushNotificationService.sendNotification("New Medical Record", "You have a new medical record from Dr. "+medicalRecords.getDoctor().getName(), token.getToken());
            }

            RecordResponse recordResponse = new RecordResponse().castToResponse(medicalRecords);

            return ResponseEntity.created(null).body(recordResponse);
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getAllRecordByUser(String sort) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try
        {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }

            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("You are not authorized to view records");
            }

            if (sort.equals("ALL")) {
                List<MedicalRecords> records = medicalRecordRepo.findByUserAndDeletedAtNull(user);
                List<RecordResponse> recordResponses = new ArrayList<>();
                for (MedicalRecords record : records
                ) {
                    recordResponses.add(new RecordResponse().castToResponse(record));
                }
                return ResponseEntity.ok().body(recordResponses);
            } else if (sort.equals("SHARED")) {

                List<ShareMedicalRecords> records = shareRecordRepo.findByUser(user);
                List<SharedRecordResponse> recordResponses = new ArrayList<>();
                for (ShareMedicalRecords record : records
                ) {
                    recordResponses.add(new SharedRecordResponse().castToResponse(record));
                }
                return ResponseEntity.ok().body(recordResponses);
            } else {
                throw new BadRequestException("sort parameter: ALL, SHARED");
            }
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

    public ResponseEntity<?> getAllRecordByDoctor(String sort) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to view records");
            }
            if (sort.equals("ALL")) {
                List<MedicalRecords> records = medicalRecordRepo.findByDoctorAndDeletedAtNull(doctor);
                List<RecordResponse> recordResponses = new ArrayList<>();
                for (MedicalRecords record : records
                ) {
                    recordResponses.add(new RecordResponse().castToResponse(record));
                }
                return ResponseEntity.ok().body(recordResponses);
            } else if (sort.equals("SHARED")) {
                List<ShareMedicalRecords> records = shareRecordRepo.findByDoctor(doctor);
                List<SharedRecordResponse> recordResponses = new ArrayList<>();
                for (ShareMedicalRecords record : records
                ) {
                    recordResponses.add(new SharedRecordResponse().castToResponse(record));
                }
                return ResponseEntity.ok().body(recordResponses);
            } else {
                throw new BadRequestException("sort parameter : ALL, SHARED");
            }
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

    public ResponseEntity<?> getRecordById(UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
            if (records.isEmpty()) {
                throw new BadRequestException("Record not found");
            }
            if (records.get().getDeletedAt() != null) {
                throw new BadRequestException("Record not found");
            }
            if (!records.get().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You are not authorized to view this record");
            }


            return ResponseEntity.ok().body(records.get());
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

    public ResponseEntity<?> updateMedicalRecord(UpdateMedicalRecordDto record, UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
            if (records.isEmpty()) {
                throw new BadRequestException("Record not found");
            }
            if (records.get().getDeletedAt() != null) {
                throw new BadRequestException("Record not found");
            }
            if (user.getRole() == UserRole.USER && records.get().isSelfAdded()){
                if (!records.get().getUser().getId().equals(user.getId())) {
                    throw new ForbiddenException("You are not authorized to update this record");
                }
            }else if (user.getRole() == UserRole.DOCTOR && !records.get().isSelfAdded()){
                if (!records.get().getDoctor().getId().equals(user.getId())) {
                    throw new ForbiddenException("You are not authorized to update this record");
                }
            }else{
                throw new ForbiddenException("You are not authorized to update this record");
            }

            if (record.getRecord() != null) {
                records.get().setRecord(ImageUtils.compress(record.getRecord().getBytes()));
            }
            if (record.getMedicalRecordType() != null) {
                records.get().setMedicalRecordType(record.getMedicalRecordType());
            }
            if (record.getRecordType() != null) {
                records.get().setRecordType(record.getRecordType());
            }
            records.get().setUpdatedAt(LocalDateTime.now());
            medicalRecordRepo.save(records.get());

            return ResponseEntity.ok().body(new RecordResponse().castToResponse(records.get()));
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

    public ResponseEntity<?> deleteMedicalRecord(UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
       try{
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           String email = auth.getName();
           Users user = userRepo.findByEmail(email);
           if (user == null) {
               throw new BadRequestException("User not found");
           }
           Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
           if (records.isEmpty()) {
               throw new BadRequestException("Record not found");
           }
           if (!records.get().getUser().getId().equals(user.getId())) {
               throw new ForbiddenException("You are not authorized to delete this record");
           }
           records.get().setDeletedAt(LocalDateTime.now());
           medicalRecordRepo.save(records.get());

           return ResponseEntity.ok().body(new SuccessResponse("Record deleted successfully"));
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

    public ResponseEntity<?> shareMedicalRecord(UUID recordId, UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        System.out.println(recordId);
        Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
        if (records.isEmpty()) {
            throw new BadRequestException("Record not found");
        }
        if (!records.get().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to share this record");
        }
        Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("Doctor not found");
        }
        // check if record is already shared
        if (shareRecordRepo.findByMedicalRecordsAndDoctor(records.get(), doctor).isPresent()) {
            throw new BadRequestException("Record already shared with this doctor");
        }
        // check if record is added by same doctor
        if (records.get().getDoctor() != null && records.get().getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("Record added by same doctor");
        }
            ShareMedicalRecords shareMedicalRecords = ShareMedicalRecords.builder()
                    .medicalRecords(records.get())
                    .doctor(doctor)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            shareRecordRepo.save(shareMedicalRecords);
            notificationService.sendNotification(shareMedicalRecords.getId(), shareMedicalRecords.getUser().getName() + " shared a medical record to you", NotificationType.SHARE_RECORD, doctor.getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(doctor)) {
                pushNotificationService.sendNotification("New Medical Record", shareMedicalRecords.getUser().getName() + " shared a medical record to you", token.getToken());
            }
            return ResponseEntity.created(null).body(new SuccessResponse("Record shared successfully"));
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

    public ResponseEntity<?> revokeMedicalRecord(UUID recordId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            Optional<ShareMedicalRecords> records = shareRecordRepo.findById(recordId);
            if (records.isEmpty()) {
                throw new BadRequestException("Record not found");
            }
            if (!records.get().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You are not authorized to revoke this record");
            }


            shareRecordRepo.delete(records.get());
            return ResponseEntity.ok().body(new SuccessResponse("Record revoked successfully"));
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
}
