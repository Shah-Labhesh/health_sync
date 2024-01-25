package com.fyp.health_sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fyp.health_sync.dtos.AddDoctorDetailsDto;
import com.fyp.health_sync.dtos.UpdateUserDto;
import com.fyp.health_sync.dtos.UploadAddressDto;
import com.fyp.health_sync.entity.Speciality;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final GeoCodingService geoCodingService;
    private final SpecialityRepo specialityRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> currentUser() throws BadRequestException, InternalServerErrorException {
       try{
           String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
           Users user = userRepo.findByEmail(currentPrincipalName);
           if (user == null) {
               throw new BadRequestException("User not found");
           }
           if (user.getStatus() != UserStatus.ACTIVE) {
               throw new BadRequestException("User is not active");
           }
           if (user.getRole() == UserRole.DOCTOR){
               return ResponseEntity.ok(new DoctorResponse().castToResponse(user));
           }else{
               return ResponseEntity.ok(new UserResponse().castToResponse(user));
           }
       }catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }


    }


    public ResponseEntity<?> uploadAddress(UUID id, UploadAddressDto address) throws BadRequestException, JsonProcessingException, InternalServerErrorException {
       try {
           Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));

           if (doctor.getRole() != UserRole.DOCTOR){
               throw  new BadRequestException("Doctor not found");
           }
           if (doctor.getAddress() != null){
               throw new BadRequestException("Address already uploaded");
           }

           doctor.setAddress(geoCodingService.getAddressFromCoordinates(address.getLatitude(), address.getLongitude()));
           doctor.setLatitude(address.getLatitude());
           doctor.setLongitude(address.getLongitude());
           userRepo.save(doctor);

           return ResponseEntity.created(null).body(new SuccessResponse("Address uploaded successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public ResponseEntity<?> uploadDetails(UUID doctorId, AddDoctorDetailsDto details) throws BadRequestException, IOException, InternalServerErrorException {
       try {
           Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
           if (doctor.getRole() != UserRole.DOCTOR){
               throw  new BadRequestException("Doctor not found");
           }
           if (doctor.getSpeciality() != null){
               throw new BadRequestException("Details already uploaded");
           }
           Speciality speciality = specialityRepo.findById(details.getSpeciality()).orElseThrow(() -> new BadRequestException("Speciality not found"));
           doctor.setSpeciality(speciality);
           doctor.setExperience(details.getExperience());
           doctor.setFee(details.getFee());
           doctor.setProfilePicture(ImageUtils.compressImage(details.getImage().getBytes()));
           userRepo.save(doctor);

           return ResponseEntity.created(null).body(new SuccessResponse("Details uploaded successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    public ResponseEntity<?> updateUser(UpdateUserDto updateUserDto) throws BadRequestException, IOException, InternalServerErrorException {
       try{
           boolean emailUpdate = false;
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           String currentPrincipalName = authentication.getName();
           Users user = userRepo.findByEmail(currentPrincipalName);
           if (user == null) {
               throw new BadRequestException("User not found");
           }

           if (updateUserDto.getName() != null) {
               user.setName(updateUserDto.getName());
           }
           if (updateUserDto.getEmail() != null) {
               user.setEmail(updateUserDto.getEmail());
               emailUpdate = true;
           }
           if (updateUserDto.getOldPassword() != null) {
               if (updateUserDto.getNewPassword() == null || updateUserDto.getNewPassword().isEmpty()) {
                   throw new BadRequestException("New password is required");
               }
               BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
               if (passwordEncoder.matches(updateUserDto.getOldPassword(), user.getPassword())) {
                   user.setPassword(passwordEncoder.encode(updateUserDto.getNewPassword()));
               } else {
                   throw new BadRequestException("Old password is incorrect");
               }
           }
           if (updateUserDto.getProfileImage() != null) {
               user.setProfilePicture(ImageUtils.compressImage(updateUserDto.getProfileImage().getBytes()));
           }
           if (user.getRole() == UserRole.DOCTOR){
               if (updateUserDto.getLatitude() > 0 || updateUserDto.getLongitude() > 0){
                   user.setLatitude(updateUserDto.getLatitude());
                   user.setLongitude(updateUserDto.getLongitude());
                   user.setAddress(geoCodingService.getAddressFromCoordinates(updateUserDto.getLatitude(), updateUserDto.getLongitude()));
               }
               if (updateUserDto.getExperience() != null){
                   user.setExperience(updateUserDto.getExperience());
               }
               if (updateUserDto.getFee() != null){
                   user.setFee(updateUserDto.getFee());
               }
               if (updateUserDto.getSpeciality() != null){
                   Speciality speciality =  specialityRepo.findById(updateUserDto.getSpeciality()).orElseThrow(() -> new BadRequestException("Speciality not found"));
                   user.setSpeciality(speciality);
               }
               if (updateUserDto.getKhaltiId() != null){
                   user.setKhaltiId(updateUserDto.getKhaltiId());
               }
           }
           user.setUpdatedAt(LocalDateTime.now());
           userRepo.save(user);
           Map<String, Object> response = new HashMap<>();
           response.put("emailUpdate", emailUpdate);
           response.put("message", "User updated successfully");

           return ResponseEntity.ok(response);
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }


}
