package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.UpdateUserDto;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.DataFormatException;

@RequiredArgsConstructor

@Service
public class UserService {

    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;

    @Transactional
    public ResponseEntity<?> currentUser() throws BadRequestException, DataFormatException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        try {
            Users use = userRepo.findByEmail(currentPrincipalName);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        Users user = userRepo.findByEmail(currentPrincipalName);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User is not active");
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture() != null ? "get-avatar/" + user.getId().toString() : null)
                .authType(user.getAuthType())
                .isVerified(user.getIsVerified())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
        return ResponseEntity.ok()
                .body(userResponse);

    }

    @Transactional // Ensure this method operates within a transaction
    public ResponseEntity<?> getProfileImage() throws BadRequestException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();

            Users user = userRepo.findByEmail(currentPrincipalName);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User is not active");
            }

            if (user.getProfilePicture() == null) {
                throw new BadRequestException("Profile picture not found");
            }

            ByteArrayResource resource = new ByteArrayResource(user.getProfilePicture());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + user.getName() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            throw new InternalServerErrorException(ex.getMessage());// Rethrow or handle the exception based on your application's logic
        }
    }


    public ResponseEntity<?> updateUser(UpdateUserDto updateUserDto) throws BadRequestException {
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
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        ProfileUpdateResponse profileUpdateResponse = new ProfileUpdateResponse();
        profileUpdateResponse.setMessage("Profile updated successfully");
        profileUpdateResponse.setEmailUpdate(emailUpdate);
        return ResponseEntity.ok(profileUpdateResponse);
    }

    @Transactional
    public ResponseEntity<?> uploadProfilePicture(MultipartFile file) throws BadRequestException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Users user = userRepo.findByEmail(currentPrincipalName);

        if (user == null) {
            throw new BadRequestException("User not found");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User is " + user.getStatus() + " cannot update profile picture");
        }

        if (file == null) {
            throw new BadRequestException("File is required");
        }
        if (user.getProfilePicture() != null) {
            user.setProfilePicture(null);
        }

        user.setProfilePicture(file.getBytes());
        userRepo.save(user);
        return ResponseEntity.ok(new SuccessResponse("Profile picture updated successfully"));
    }

}
