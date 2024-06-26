package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.AddMedicalRecordDto;
import com.fyp.health_sync.dtos.UpdateMedicalRecordDto;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.ShareMedicalRecords;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.MedicalRecordRepo;
import com.fyp.health_sync.repository.ShareRecordRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepo medicalRecordRepo;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;
    private final ShareRecordRepo shareRecordRepo;

    public ResponseEntity<?> uploadRecord(AddMedicalRecordDto recordDto) throws BadRequestException, IOException, InternalServerErrorException {
       try{
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           String email = auth.getName();
           Users user = userRepo.findByEmail(email);
           if (user == null) {
               throw  new BadRequestException("User not found");
           }
           MedicalRecords medicalRecords = MedicalRecords.builder()
                   .recordType(recordDto.getRecordType())
                   .record(recordDto.getRecord().getBytes())
                   .recordText(recordDto.getRecordText())
                   .selfAdded(true)
                   .user(user)
                   .createdAt(LocalDateTime.now())
                   .build();
           medicalRecordRepo.save(medicalRecords);

           return ResponseEntity.created(null).body(new SuccessResponse("Record added successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public  ResponseEntity<?> uploadRecordByDoctor(AddMedicalRecordDto recordDto, UUID userId) throws BadRequestException, IOException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            Doctors doctor = doctorRepo.findByEmail(email);
            if (doctor == null) {
                throw  new BadRequestException("Doctor not found");
            }
            Users patient = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
            MedicalRecords medicalRecords = MedicalRecords.builder()
                    .recordType(recordDto.getRecordType())
                    .record(recordDto.getRecord().getBytes())
                    .recordText(recordDto.getRecordText())
                    .selfAdded(false)
                    .user(patient)
                    .doctor(doctor)
                    .createdAt(LocalDateTime.now())
                    .build();
            medicalRecordRepo.save(medicalRecords);

            return ResponseEntity.created(null).body(new SuccessResponse("Record added successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
    public ResponseEntity<?> getAllRecord() throws BadRequestException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw  new BadRequestException("User not found");
        }

        return ResponseEntity.ok().body(medicalRecordRepo.findByUserIdAndDeletedAtNotNull(user.getId()));

    }

    public ResponseEntity<?> getRecordById(UUID recordId) throws BadRequestException, ForbiddenException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw  new BadRequestException("User not found");
        }
        Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
        if (records.isEmpty() ) {
            throw new BadRequestException("Record not found");
        }
        if (records.get().getDeletedAt() != null) {
            throw new BadRequestException("Record not found");
        }
        if(!records.get().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to view this record");
        }


        return ResponseEntity.ok().body(records.get());

    }

    public ResponseEntity<?> updateMedicalRecord(UpdateMedicalRecordDto record, UUID recordId) throws BadRequestException, ForbiddenException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw  new BadRequestException("User not found");
        }
        Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
        if (records.isEmpty()) {
            throw new BadRequestException("Record not found");
        }
        if(!records.get().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to update this record");
        }
        if (record.getRecord() != null) {
            records.get().setRecord(record.getRecord().getBytes());
        }
        if (record.getRecordText() != null) {
            records.get().setRecordText(record.getRecordText());
        }
        if (record.getRecordType() != null) {
            records.get().setRecordType(record.getRecordType());
        }
        records.get().setUpdatedAt(LocalDateTime.now());
        medicalRecordRepo.save(records.get());
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage("Record Updated successfully");
        return ResponseEntity.ok().body(successResponse);
    }

    public ResponseEntity<?> deleteMedicalRecord(UUID recordId) throws BadRequestException, ForbiddenException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw  new BadRequestException("User not found");
        }
        Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
        if (records.isEmpty()) {
            throw new BadRequestException("Record not found");
        }
        if(!records.get().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to delete this record");
        }
        records.get().setDeletedAt(LocalDateTime.now());
        medicalRecordRepo.save(records.get());
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage("Record Deleted successfully");
        return ResponseEntity.ok().body(successResponse);
    }

    public ResponseEntity<?> shareMedicalRecord(UUID recordId, UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw  new BadRequestException("User not found");
        }
        Optional<MedicalRecords> records = medicalRecordRepo.findById(recordId);
        if (records.isEmpty()) {
            throw new BadRequestException("Record not found");
        }
        if(!records.get().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to share this record");
        }
        Optional<Doctors> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()) {
            throw new BadRequestException("Doctor not found");
        }
        try{
            ShareMedicalRecords shareMedicalRecords = ShareMedicalRecords.builder()
                    .medicalRecords(records.get())
                    .doctor(doctor.get())
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            shareRecordRepo.save(shareMedicalRecords);

            return ResponseEntity.ok().body(new SuccessResponse("Record shared successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
