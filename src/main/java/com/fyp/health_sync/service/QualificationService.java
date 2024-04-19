package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.QualificationDto;
import com.fyp.health_sync.dtos.UpdateQualificationDto;
import com.fyp.health_sync.entity.Qualifications;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.QualificationRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ImageUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class QualificationService {
    private final QualificationRepo qualificationRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> addQualification(QualificationDto qualification, UUID doctorId)
            throws BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(doctorId)
                    .orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BadRequestException("You are not authorized to add qualification");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

            LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);

            Qualifications qualifications = Qualifications.builder()
                    .qualification(qualification.getTitle())
                    .institute(qualification.getInstitute())
                    .passOutYear(dateTime)
                    .certificate(ImageUtils.compress(qualification.getCertificate().getBytes()))
                    .doctor(doctor)
                    .build();
            qualificationRepo.save(qualifications);
            return ResponseEntity.created(null).body(new QualificationResponse().castToResponse(qualifications));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


    public ResponseEntity<?> saveQualificationAuth(QualificationDto qualification) throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to add qualification");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);

            Qualifications qualifications = Qualifications.builder()
                    .qualification(qualification.getTitle())
                    .institute(qualification.getInstitute())
                    .passOutYear(dateTime)
                    .certificate(ImageUtils.compress(qualification.getCertificate().getBytes()))
                    .doctor(doctor)
                    .build();
            qualificationRepo.save(qualifications);
            return ResponseEntity.created(null).body(new QualificationResponse().castToResponse(qualifications));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteQualification(UUID qualificationId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to delete qualification");
            }
            Qualifications qualification = qualificationRepo.findById(qualificationId)
                    .orElseThrow(() -> new BadRequestException("Qualification not found"));
            if (!qualification.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to delete this qualification");
            }
            qualificationRepo.delete(qualification);

            return ResponseEntity.ok().body(new SuccessResponse("Qualification deleted successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateQualification(UUID qualificationId, UpdateQualificationDto qualification) throws BadRequestException, ForbiddenException,  InternalServerErrorException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to update qualification");
            }
            Qualifications qualification1 = qualificationRepo.findById(qualificationId)
                    .orElseThrow(() -> new BadRequestException("Qualification not found"));
            if (!qualification1.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to update this qualification");
            }
            if (qualification.getQualification() != null) {
                qualification1.setQualification(qualification.getQualification());
            }
            if (qualification.getInstitute() != null) {
                qualification1.setInstitute(qualification.getInstitute());
            }
            if (qualification.getPassOutYear() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

                LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);
                qualification1.setPassOutYear(dateTime);
            }
            if (qualification.getCertificate() != null) {
                qualification1.setCertificate(ImageUtils.compress(qualification.getCertificate().getBytes()));
            }
            qualificationRepo.save(qualification1);

            return ResponseEntity.ok().body(new QualificationResponse().castToResponse(qualification1));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateQualificationByDoctorId(UUID doctorId, UUID qualificationId,
                                                           UpdateQualificationDto qualification) throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to update qualification");
            }
            Qualifications qualification1 = qualificationRepo.findById(qualificationId)
                    .orElseThrow(() -> new BadRequestException("Qualification not found"));
            if (!qualification1.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to update this qualification");
            }
            if (qualification.getQualification() != null) {
                qualification1.setQualification(qualification.getQualification());
            }
            if (qualification.getInstitute() != null) {
                qualification1.setInstitute(qualification.getInstitute());
            }
            if (qualification.getPassOutYear() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

                LocalDateTime dateTime = LocalDateTime.parse(qualification.getPassOutYear(), formatter);
                qualification1.setPassOutYear(dateTime);
            }
            if (qualification.getCertificate() != null) {
                qualification1.setCertificate(ImageUtils.compress(qualification.getCertificate().getBytes()));
            }
            qualificationRepo.save(qualification1);
            return ResponseEntity.ok().body(new QualificationResponse().castToResponse(qualification1));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteQualificationByDoctorId(UUID doctorId, UUID qualificationId)
            throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to delete qualification");
            }
            Qualifications qualification = qualificationRepo.findById(qualificationId)
                    .orElseThrow(() -> new BadRequestException("Qualification not found"));
            if (!qualification.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to delete this qualification");
            }
            qualificationRepo.deleteQualificationsById(qualificationId);
            return ResponseEntity.ok().body(new SuccessResponse("Qualification deleted successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getMyQualification() throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to get qualification");
            }
            List<Qualifications> qualification = qualificationRepo.findAllByDoctorId(doctor.getId());
            List<QualificationResponse> qualificationResponseList = new ArrayList<>();
            for (Qualifications qualifications : qualification) {

                qualificationResponseList.add(new QualificationResponse().castToResponse(qualifications));
            }
            return ResponseEntity.ok().body(qualificationResponseList);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getQualificationById(UUID doctorId) throws BadRequestException, ForbiddenException, InternalServerErrorException {

        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to get qualification");
            }

            List<Qualifications> qualification = qualificationRepo.findAllByDoctorId(doctorId);
            List<QualificationResponse> qualificationResponseList = new ArrayList<>();
            for (Qualifications qualifications : qualification) {

                qualificationResponseList.add(new QualificationResponse().castToResponse(qualifications));
            }


            return ResponseEntity.ok().body(qualificationResponseList);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
