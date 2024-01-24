package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.Qualifications;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.MedicalRecordRepo;
import com.fyp.health_sync.repository.QualificationRepo;
import com.fyp.health_sync.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MultipartFileService {

    private  final QualificationRepo qualificationRepo;
    private final MedicalRecordRepo medicalRecordRepo;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;

    public ResponseEntity<?> getAvatarById(UUID id) throws BadRequestException {
        Users user = userRepo.findById(id).orElse(null);
        Doctors doc = doctorRepo.findById(id).orElse(null);

        if (user != null) {
            return buildImageResponse(user.getProfilePicture(), user.getName());
        } else if (doc != null) {
            return buildImageResponse(doc.getImage(), doc.getName());
        } else {
            throw new BadRequestException("Avatar not found");
        }
    }

    private ResponseEntity<?> buildImageResponse(byte[] imageBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }




    public ResponseEntity<?> getCertificate(UUID id) throws BadRequestException {

            Qualifications qualifications = qualificationRepo.findById(id).orElseThrow( () -> new BadRequestException("Qualification not found"));



            return buildImageResponse(qualifications.getCertificate(), qualifications.getInstitute());

    }

    public ResponseEntity<?> getImageRecord(UUID id) throws BadRequestException {

        MedicalRecords medicalRecord = medicalRecordRepo.findById(id).orElseThrow( () -> new BadRequestException("Record not found"));


        return buildImageResponse(medicalRecord.getRecord(), medicalRecord.getId().toString());

    }

    public ResponseEntity<?> getPdfRecord(UUID id) throws BadRequestException {

        MedicalRecords medicalRecord = medicalRecordRepo.findById(id).orElseThrow( () -> new BadRequestException("Record not found"));

        ByteArrayResource resource = new ByteArrayResource(medicalRecord.getRecord());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + medicalRecord.getId() + "\"")
                .body(resource);

    }



}
