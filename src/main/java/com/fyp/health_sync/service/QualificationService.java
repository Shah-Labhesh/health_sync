package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.AddMoreDetailsDto;
import com.fyp.health_sync.dtos.QualificationDto;
import com.fyp.health_sync.dtos.UpdateQualificationDto;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Qualifications;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.QualificationRepo;
import com.fyp.health_sync.utils.QualificationResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class QualificationService {
    private final QualificationRepo qualificationRepo;
    private final DoctorRepo doctorRepo;

    public ResponseEntity<?> addQualification(QualificationDto qualification, UUID doctorId)
            throws BadRequestException, InternalServerErrorException {
        try {
            Doctors doctor = doctorRepo.findById(doctorId)
                    .orElseThrow(() -> new BadRequestException("Doctor not found"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

            LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);

            Qualifications qualifications = Qualifications.builder()
                    .qualification(qualification.getTitle())
                    .institute(qualification.getInstitute())
                    .passOutYear(dateTime)
                    .certificate(qualification.getCertificate().getBytes())
                    .doctorId(doctor)
                    .build();
            qualificationRepo.save(qualifications);
            return ResponseEntity.created(null).body(new SuccessResponse("Qualification added successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> saveKhalti(AddMoreDetailsDto details, UUID doctorId)
            throws BadRequestException, InternalServerErrorException {
        Doctors doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
        try {

            doctor.setKhaltiId(details.getKhaltiId());
            doctorRepo.save(doctor);
            return ResponseEntity.created(null).body(new SuccessResponse("Details added successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> saveQualificationAuth(QualificationDto qualification) throws BadRequestException, InternalServerErrorException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Doctors doctor = doctorRepo.findByEmail(email);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);

            Qualifications qualifications = Qualifications.builder()
                    .qualification(qualification.getTitle())
                    .institute(qualification.getInstitute())
                    .passOutYear(dateTime)
                    .doctorId(doctor)
                    .build();
            qualificationRepo.save(qualifications);
            return ResponseEntity.ok().body(new SuccessResponse("Qualification added successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

//    public ResponseEntity<?> getQualification(UUID doctorId) throws BadRequestException {
//        Doctors doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
//        return ResponseEntity.ok().body(qualificationRepo.findByDoctorIdId(doctor.getId()));
//    }

    public ResponseEntity<?> getQualificationAuth() throws BadRequestException, InternalServerErrorException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Doctors doctor = doctorRepo.findByEmail(email);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
       try {
           List<Qualifications> qualification = qualificationRepo.findByDoctorIdId(doctor.getId());
           List<QualificationResponse> qualificationResponseList = new ArrayList<>();
           for (Qualifications qualifications : qualification) {

               qualificationResponseList.add(new QualificationResponse().castToResponse(qualifications));
           }
           return ResponseEntity.ok().body(qualificationResponseList);
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    @Transactional
    public ResponseEntity<?> deleteQualification(UUID qualificationId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctors doctor = doctorRepo.findByEmail(email);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        Qualifications qualification = qualificationRepo.findById(qualificationId)
                .orElseThrow(() -> new BadRequestException("Qualification not found"));
        if (!qualification.getDoctorId().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to delete this qualification");
        }
        try {
            qualificationRepo.delete(qualification);

            return ResponseEntity.ok().body(new SuccessResponse("Qualification deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateQualification(UUID qualificationId, UpdateQualificationDto qualification)
            throws BadRequestException, ForbiddenException, IOException, InternalServerErrorException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctors doctor = doctorRepo.findByEmail(email);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        Qualifications qualification1 = qualificationRepo.findById(qualificationId)
                .orElseThrow(() -> new BadRequestException("Qualification not found"));
        if (!qualification1.getDoctorId().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to update this qualification");
        }
        if (qualification.getQualification() != null) {
            qualification1.setQualification(qualification.getQualification());
        }
        if (qualification.getInstitute() != null) {
            qualification1.setInstitute(qualification.getInstitute());
        }
        if (qualification.getPassOutYear() != null) {
            qualification1.setPassOutYear(qualification.getPassOutYear());
        }
        if (qualification.getCertificate() != null) {
            qualification1.setCertificate(qualification.getCertificate().getBytes());
        }
        try{
            qualificationRepo.save(qualification1);

            return ResponseEntity.ok().body(new SuccessResponse("Qualification updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateQualificationByDoctorId(UUID doctorId, UUID qualificationId,
            UpdateQualificationDto qualification) throws ForbiddenException, BadRequestException, IOException, InternalServerErrorException {
        Doctors doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
        Qualifications qualification1 = qualificationRepo.findById(qualificationId)
                .orElseThrow(() -> new BadRequestException("Qualification not found"));
        if (!qualification1.getDoctorId().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to update this qualification");
        }
        if (qualification.getQualification() != null) {
            qualification1.setQualification(qualification.getQualification());
        }
        if (qualification.getInstitute() != null) {
            qualification1.setInstitute(qualification.getInstitute());
        }
        if (qualification.getPassOutYear() != null) {
            qualification1.setPassOutYear(qualification.getPassOutYear());
        }
        if (qualification.getCertificate() != null) {
            qualification1.setCertificate(qualification.getCertificate().getBytes());
        }
        try{
            qualificationRepo.save(qualification1);
            return ResponseEntity.ok().body(new QualificationResponse().castToResponse(qualification1));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteQualificationByDoctorId(UUID doctorId, UUID qualificationId)
            throws ForbiddenException, BadRequestException, InternalServerErrorException {
        Doctors doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
        Qualifications qualification = qualificationRepo.findById(qualificationId)
                .orElseThrow(() -> new BadRequestException("Qualification not found"));
        if (!qualification.getDoctorId().getId().equals(doctor.getId())) {
            throw new ForbiddenException("You are not authorized to delete this qualification");
        }
       try{
           qualificationRepo.deleteQualificationsById(qualificationId);
           return ResponseEntity.ok().body(new SuccessResponse("Qualification deleted successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public ResponseEntity<?> getMyQualification() throws BadRequestException, InternalServerErrorException {
       try {
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           String email = auth.getName();
           Doctors doctor = doctorRepo.findByEmail(email);
           if (doctor == null) {
               throw new BadRequestException("Doctor not found");
           }
           List<Qualifications> qualification = qualificationRepo.findByDoctorIdId(doctor.getId());
           List<QualificationResponse> qualificationResponseList = new ArrayList<>();
           for (Qualifications qualifications : qualification) {

               qualificationResponseList.add(new QualificationResponse().castToResponse(qualifications));
           }
           return ResponseEntity.ok().body(qualificationResponseList);
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public ResponseEntity<?> getQualificationById(UUID doctorId) throws BadRequestException, InternalServerErrorException {
       
        try{
            Optional<Doctors> doctor = doctorRepo.findById(doctorId);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }

            List<Qualifications> qualification = qualificationRepo.findByDoctorIdId(doctorId);
            List<QualificationResponse> qualificationResponseList = new ArrayList<>();
            for (Qualifications qualifications : qualification) {

                qualificationResponseList.add(new QualificationResponse().castToResponse(qualifications));
            }


            return ResponseEntity.ok().body(qualificationResponseList);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
