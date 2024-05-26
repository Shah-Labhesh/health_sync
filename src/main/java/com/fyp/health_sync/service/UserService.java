package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.*;
import com.fyp.health_sync.entity.DataRemovalRequest;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Speciality;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DataRemovalRequestRepo;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.DoctorResponse;
import com.fyp.health_sync.utils.ImageUtils;
import com.fyp.health_sync.utils.SuccessResponse;
import com.fyp.health_sync.utils.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    
    private final SpecialityRepo specialityRepo;
    private final UserRepo userRepo;
    private final FirebaseTokenRepo firebaseTokenRepo;
    private final DataRemovalRequestRepo dataRemovalRequestRepo;
    private final MailService mailService;

    public ResponseEntity<?> currentUser() throws BadRequestException, InternalServerErrorException {
        try {
            String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(currentPrincipalName);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User is " + user.getStatus());
            }
            if (user.getRole() == UserRole.DOCTOR) {
                return ResponseEntity.ok(new DoctorResponse().castToResponse(user));
            } else {
                return ResponseEntity.ok(new UserResponse().castToResponse(user));
            }
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> uploadAddress(UUID id, UploadAddressDto address)
            throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));

            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to upload address");
            }
            if (doctor.getAddress() != null) {
                throw new BadRequestException("Address already uploaded");
            }

            doctor.setLatitude(address.getLatitude());
            doctor.setLongitude(address.getLongitude());
            userRepo.save(doctor);

            return ResponseEntity.created(null).body(new SuccessResponse("Address uploaded successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> uploadDetails(UUID doctorId, AddDoctorDetailsDto details)
            throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to upload details");
            }
            if (doctor.getSpeciality() != null) {
                throw new BadRequestException("Details already uploaded");
            }
            Speciality speciality = specialityRepo.findById(details.getSpeciality())
                    .orElseThrow(() -> new BadRequestException("Speciality not found"));
            doctor.setSpeciality(speciality);
            doctor.setExperience(details.getExperience());
            doctor.setFee(details.getFee());
            doctor.setProfilePicture(ImageUtils.compress(details.getImage().getBytes()));
            userRepo.save(doctor);

            return ResponseEntity.created(null).body(new SuccessResponse("Details uploaded successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> saveKhalti(AddMoreDetailsDto details, UUID doctorId)
            throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to add qualification");
            }
            doctor.setKhaltiId(details.getKhaltiId());
            userRepo.save(doctor);
            return ResponseEntity.created(null).body(new SuccessResponse("Details added successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        }  catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateUser(UpdateUserDto updateUserDto)
            throws BadRequestException, InternalServerErrorException {
        try {
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
                Users existUsers = userRepo.findByEmail(updateUserDto.getEmail());
                if (existUsers != null) {
                    throw new BadRequestException("Email already exists");
                }
                user.setEmail(updateUserDto.getEmail());
                user.setIsVerified(false);
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
                user.setProfilePicture(ImageUtils.compress(updateUserDto.getProfileImage().getBytes()));
            }
            if (user.getRole() == UserRole.DOCTOR) {
                if (updateUserDto.getLatitude() > 0 || updateUserDto.getLongitude() > 0) {
                    user.setLatitude(updateUserDto.getLatitude());
                    user.setLongitude(updateUserDto.getLongitude());
                }
                if (updateUserDto.getExperience() != null) {
                    user.setExperience(updateUserDto.getExperience());
                }
                if (updateUserDto.getFee() != null) {
                    user.setFee(updateUserDto.getFee());
                }
                if (updateUserDto.getSpeciality() != null) {
                    Speciality speciality = specialityRepo.findById(updateUserDto.getSpeciality())
                            .orElseThrow(() -> new BadRequestException("Speciality not found"));
                    user.setSpeciality(speciality);
                }
                if (updateUserDto.getKhaltiId() != null) {
                    user.setKhaltiId(updateUserDto.getKhaltiId());
                }
            }
            if (updateUserDto.getTextNotification() != null) {
                user.setTextNotification(updateUserDto.getTextNotification());
            }
            user.setUpdatedAt(LocalDateTime.now());
            userRepo.save(user);
            Map<String, Object> response = new HashMap<>();
            response.put("emailUpdate", emailUpdate);
            response.put("message", "User updated successfully");

            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> saveFirebaseToken(String token) throws BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            FirebaseToken firebaseToken = firebaseTokenRepo.findByTokenAndUserIsNotNull(token);
            if (firebaseToken == null) {
                firebaseToken = FirebaseToken.builder()
                        .token(token)
                        .user(user)
                        .build();
            } else {
                firebaseToken.setUser(user);
            }
            firebaseTokenRepo.save(firebaseToken);
            return ResponseEntity.created(null).body(new SuccessResponse("Token saved successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteFirebaseToken(String token)
            throws BadRequestException, InternalServerErrorException {
        try {
            FirebaseToken firebaseToken = firebaseTokenRepo.findByTokenAndUserIsNotNull(token);
            if (firebaseToken == null) {
                return ResponseEntity.ok().body(new SuccessResponse("Token deleted successfully"));
            }
            firebaseTokenRepo.delete(firebaseToken);
            return ResponseEntity.ok().body(new SuccessResponse("Token deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> requestForDataRemoval(DataRemovalRequestDto request) throws BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            List<DataRemovalRequest> removalRequest = dataRemovalRequestRepo.findAllByUserAndType(user,request.getType().toString());
            for ( DataRemovalRequest dataRemovalRequest : removalRequest) {
                if (!dataRemovalRequest.isAccepted() && !dataRemovalRequest.isRejected()) {
                    throw new BadRequestException("Request already sent");
                }
            }
            DataRemovalRequest dataRemovalRequest = DataRemovalRequest.builder()
                    .user(user)
                    .reason(request.getReason())
                    .type(request.getType())
                    .isAccepted(false)
                    .isRejected(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            dataRemovalRequestRepo.save(dataRemovalRequest);
            mailService.sendRequestEmail(request.getType().toString(), request.getReason(), user.getName());
            return ResponseEntity.created(null).body(new SuccessResponse("Request sent successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getRequests() throws BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            List<DataRemovalRequest> removalRequests = dataRemovalRequestRepo.findAllByUser(user);
            for (DataRemovalRequest removalRequest : removalRequests) {
                removalRequest.setUser(null);
            }
            return ResponseEntity.ok(removalRequests);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> requestForApproval() throws BadRequestException, InternalServerErrorException, ForbiddenException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to request for approval");
            }
            if (user.getApproved()) {
                throw new BadRequestException("Your account is already approved");
            }
            mailService.sendApprovalRequestEmail(user.getName());
            return ResponseEntity.created(null).body(new SuccessResponse("Request sent successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        }

        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
