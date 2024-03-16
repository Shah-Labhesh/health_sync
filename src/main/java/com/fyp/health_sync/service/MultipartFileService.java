package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.*;
import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.*;
import com.fyp.health_sync.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.zip.DataFormatException;

@Service
@RequiredArgsConstructor
public class MultipartFileService {

    private final QualificationRepo qualificationRepo;
    private final MedicalRecordRepo medicalRecordRepo;
    private final UserRepo userRepo;
    private final SpecialityRepo specialityRepo;
    private final PrescriptionRepo prescriptionRepo;

    public ResponseEntity<?> getAvatarById(UUID id) throws BadRequestException, InternalServerErrorException {
        try {
            Users user = userRepo.findById(id).orElseThrow(() -> new BadRequestException("User not found"));

            return buildImageResponse(ImageUtils.decompress(user.getProfilePicture()), user.getName());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    private ResponseEntity<?> buildImageResponse(byte[] imageBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(imageBytes);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }


    public ResponseEntity<?> getCertificate(UUID id) throws BadRequestException,  InternalServerErrorException {
        try {
            Qualifications qualifications = qualificationRepo.findById(id).orElseThrow(() -> new BadRequestException("Qualification not found"));
            return buildImageResponse(ImageUtils.decompress(qualifications.getCertificate()), qualifications.getInstitute());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getImageRecord(UUID id) throws BadRequestException, DataFormatException, InternalServerErrorException {
try {
    MedicalRecords medicalRecord = medicalRecordRepo.findById(id).orElseThrow(() -> new BadRequestException("Record not found"));
    return buildImageResponse(ImageUtils.decompress(medicalRecord.getRecord()), medicalRecord.getId().toString());
}
catch (BadRequestException e) {
    throw new BadRequestException(e.getMessage());
}
catch (Exception e) {
    throw new InternalServerErrorException(e.getMessage());
}
    }

    public ResponseEntity<?> getPdfRecord(UUID id) throws BadRequestException, InternalServerErrorException {

        try {
            MedicalRecords medicalRecord = medicalRecordRepo.findById(id).orElseThrow(() -> new BadRequestException("Record not found"));
            ByteArrayResource resource = new ByteArrayResource(ImageUtils.decompress(medicalRecord.getRecord()));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + medicalRecord.getUser().getName() + "\"")
                    .body(resource);
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getSpecialityImage(UUID id) throws BadRequestException, InternalServerErrorException {

        try {

            Speciality speciality = specialityRepo.findById(id).orElseThrow(() -> new BadRequestException("Speciality not found"));

            return buildImageResponse(ImageUtils.decompress(speciality.getImage()), speciality.getName());
        }
        catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getPrescription(UUID id) throws BadRequestException, InternalServerErrorException {
       try{
           Prescriptions prescription = prescriptionRepo.findById(id).orElseThrow(() -> new BadRequestException("Prescription not found"));
           if (prescription.getRecordType().equals(RecordType.IMAGE.name())) {
               return buildImageResponse(prescription.getPrescription(), prescription.getUser().getName());
           } else {
               ByteArrayResource resource = new ByteArrayResource(prescription.getPrescription());

               return ResponseEntity.ok()
                       .contentType(MediaType.APPLICATION_PDF)
                       .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prescription.getUser().getName() + "\"")
                       .body(resource);
           }
       }
         catch (BadRequestException e) {
              throw new BadRequestException(e.getMessage());
         }
         catch (Exception e) {
              throw new InternalServerErrorException(e.getMessage());
         }


    }

}
