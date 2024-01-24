package com.fyp.health_sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.AddDoctorDetailsDto;
import com.fyp.health_sync.dtos.UpdateDoctorDto;
import com.fyp.health_sync.dtos.UploadAddressDto;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Speciality;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.utils.ProfileUpdateResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

   private final DoctorRepo doctorRepo;
   private final GeoCodingService geoCodingService;
   private final SpecialityRepo specialityRepo;
    public ResponseEntity<?> uploadAddress(UUID doctorId, UploadAddressDto address) throws BadRequestException, JsonProcessingException {
        Optional<Doctors> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()){
            throw  new BadRequestException("Doctor not found");
        }
        if (doctor.get().getAddress() != null){
            throw new BadRequestException("Address already uploaded");
        }


        doctor.get().setAddress(geoCodingService.getAddressFromCoordinates(address.getLatitude(), address.getLongitude()));
        doctor.get().setLatitude(address.getLatitude());
        doctor.get().setLongitude(address.getLongitude());
        doctorRepo.save(doctor.get());
        SuccessResponse successResponse = new SuccessResponse();

        successResponse.setMessage("Address uploaded successfully");
        return ResponseEntity.created(null).body(successResponse);
    }

    public ResponseEntity<?> uploadDetails(UUID doctorId, AddDoctorDetailsDto details) throws BadRequestException, IOException {
        Optional<Doctors> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()){
            throw  new BadRequestException("Doctor not found");
        }
        if (doctor.get().getSpeciality() != null){
            throw new BadRequestException("Details already uploaded");
        }
        Speciality speciality = specialityRepo.findById(details.getSpeciality()).orElseThrow(() -> new BadRequestException("Speciality not found"));
        doctor.get().setSpeciality(speciality);
        doctor.get().setExperience(details.getExperience());
        doctor.get().setFee(details.getFee());
        doctor.get().setImage(details.getImage().getBytes());
        doctorRepo.save(doctor.get());
        SuccessResponse successResponse = new SuccessResponse();

        successResponse.setMessage("Details uploaded successfully");
        return ResponseEntity.created(null).body(successResponse);
    }

    public ResponseEntity<?> currentDoctor() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        return ResponseEntity.ok(doctor);
    }

    public ResponseEntity<?> getDoctorDetails(UUID doctorId) throws BadRequestException {
        Optional<Doctors> doctor = doctorRepo.findById(doctorId);
        if (doctor.isEmpty()){
            throw  new BadRequestException("Doctor not found");
        }
        return ResponseEntity.ok(doctor.get());
    }

    public ResponseEntity<?> getDoctorDetailsAuth() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doctor = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }

        return ResponseEntity.ok(doctor);
    }

    public ResponseEntity<?> updateDoctorDetails(UpdateDoctorDto doctor) throws BadRequestException {
        Boolean emailUpdate = false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Doctors doc = doctorRepo.findByEmail(currentPrincipalName);
        if (doctor == null) {
            throw new BadRequestException("Doctor not found");
        }
        if (doctor.getName() != null){
            doc.setName(doctor.getName());
        }
        if (doctor.getEmail() != null){
            doc.setEmail(doctor.getEmail());
            emailUpdate = true;
        }

        if (doctor.getOldPassword() != null){
            if (doctor.getNewPassword() == null || doctor.getNewPassword().isEmpty()){
                throw new BadRequestException("New password is required");
            }
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if(encoder.matches(doctor.getOldPassword(), doc.getPassword())){
                doc.setPassword( encoder.encode(doctor.getNewPassword()));
            }
            else {
                throw new BadRequestException("Old password is incorrect");
            }

        }
        if (doctor.getExperience() != null){
            doc.setExperience(doctor.getExperience());
        }
        if (doctor.getFee() != null){
            doc.setFee(doctor.getFee());
        }
        if (doctor.getSpeciality() != null){
          Speciality speciality =  specialityRepo.findById(doctor.getSpeciality()).orElseThrow(() -> new BadRequestException("Speciality not found"));
            doc.setSpeciality(speciality);
        }
        if (doctor.getImage() != null){
            doc.setImage(doctor.getImage());
        }
        if (doctor.getAddress() != null){
            doc.setAddress(doctor.getAddress());
        }
        if (doctor.getLatitude() != doc.getLatitude()){
            doc.setLatitude(doctor.getLatitude());
        }
        if (doctor.getLongitude() != doc.getLongitude()){
            doc.setLongitude(doctor.getLongitude());
        }
        if (doctor.getKhaltiId() != null){
            doc.setKhaltiId(doctor.getKhaltiId());
        }
        doc.setUpdatedAt(LocalDateTime.now());
        doctorRepo.save(doc);

        ProfileUpdateResponse response = new ProfileUpdateResponse();
        response.setMessage("Details updated successfully");
        response.setEmailUpdate(emailUpdate);
        return ResponseEntity.ok(response);
    }

}
