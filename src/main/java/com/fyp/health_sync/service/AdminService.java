package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.DataRemovalRequest;
import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.Prescriptions;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DataRemovalRequestRepo;
import com.fyp.health_sync.repository.MedicalRecordRepo;
import com.fyp.health_sync.repository.PrescriptionRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.DoctorResponse;
import com.fyp.health_sync.utils.RemovalRequestResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import com.fyp.health_sync.utils.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepo userRepo;
    private final DataRemovalRequestRepo dataRemovalRequestRepo;
    private final MedicalRecordRepo medicalRecordRepo;
    private final PrescriptionRepo prescriptionRepo;
    private final MailService mailService;

//    public ResponseEntity<?> getAllUsers(Integer pageNumber, Integer pageSize) throws InternalServerErrorException {
//        try {
//            Pageable p = PageRequest.of(pageNumber, pageSize);
//            Slice<Users> users = userRepo.findAllByRole(UserRole.USER, p);
//            List<UserResponse> response = new ArrayList<>();
//            for (Users user : users) {
//                response.add(new UserResponse().castToResponse(user));
//            }
//            return ResponseEntity.ok(users);
//        } catch (Exception e) {
//            throw new InternalServerErrorException(e.getMessage());
//        }
//    }

    public ResponseEntity<?> updateApprovedStatus(UUID id, Boolean status, String message)
            throws BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));

            if (doctor.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Doctor account is not active");
            }
            

            if (!status) {
                if (message == null || message.isEmpty()) {
                    throw new BadRequestException("Message is required for rejection");
                } else {
                    mailService.sendApprovalRejectEmail(doctor.getEmail(), "Dr. " + doctor.getName(), message);
                    return ResponseEntity.ok(new SuccessResponse("Rejection message sent successfully"));
                }
            }

            doctor.setApproved(true);
            userRepo.save(doctor);

            return ResponseEntity.ok(new SuccessResponse("Approval Status updated successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updatePopularStatus(UUID id, Boolean status)
            throws BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));

            if (doctor.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Doctor account is not active");
            }
            if (!doctor.getApproved()) {
                throw new BadRequestException("Doctor not approved yet");
            }
            if (doctor.isPopular() == status) {
                throw new BadRequestException("Status already updated");
            }

            doctor.setPopular(status);
            userRepo.save(doctor);

            return ResponseEntity.ok(new SuccessResponse("Popular Status updated successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete user
    public ResponseEntity<?> changeAccountStatus(UUID id, UserStatus status)
            throws BadRequestException, InternalServerErrorException {
        try {
            Users user = userRepo.findById(id).orElseThrow(() -> new BadRequestException("User not found"));

            if (user.getStatus() == UserStatus.DELETED) {
                throw new BadRequestException("User already deleted");
            }
            if (status == UserStatus.DELETED) {
                user.setDeletedAt(LocalDateTime.now());
                user.setStatus(UserStatus.DELETED);
            } else {
                user.setStatus(status);
            }

            userRepo.save(user);

            return ResponseEntity.ok(new SuccessResponse("Account status updated successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete user permanently
    @Transactional
    public ResponseEntity<?> deleteUser(UUID userId) throws BadRequestException, InternalServerErrorException {
        try {
            Users user = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

            if (user.getStatus() != UserStatus.DELETED) {
                throw new BadRequestException("User not found in trash");
            }

            userRepo.delete(user);

            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // restore user
    public ResponseEntity<?> restoreUser(UUID userId) throws BadRequestException, InternalServerErrorException {
        try {
            Users user = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));

            if (user.getStatus() != UserStatus.DELETED) {
                throw new BadRequestException("User not found in trash");
            }

            user.setDeletedAt(null);
            user.setStatus(UserStatus.ACTIVE);
            userRepo.save(user);

            return ResponseEntity.ok(new SuccessResponse("User restored successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    // getAllSoftDeletedUsers
    private List<UserResponse> getUsersByStatus(UserStatus status) throws InternalServerErrorException {
        try {
            List<Users> users = userRepo.findAllByStatusAndRole(status, UserRole.USER);
            List<UserResponse> response = new ArrayList<>();
            for (Users user : users) {
                response.add(new UserResponse().castToResponse(user));
            }

            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public List<DoctorResponse> getDoctorsByStatus(UserStatus status) throws InternalServerErrorException {
        try {
            List<Users> users = userRepo.findAllByStatusAndRoleAndApproved(status, UserRole.DOCTOR, true);
            List<DoctorResponse> response = new ArrayList<>();
            for (Users user : users) {
                response.add(new DoctorResponse().castToResponse(user));
            }
            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // getAllSoftDeletedDoctors

    public List<DoctorResponse> getAllUnapprovedDoctors() throws InternalServerErrorException {
        try {
            List<Users> doctors = userRepo.findAllByApprovedFalseAndRole(UserRole.DOCTOR);
            List<DoctorResponse> response = new ArrayList<>();

            for (Users doctor : doctors) {

                if (doctor.getStatus() == UserStatus.ACTIVE) {

                    response.add(new DoctorResponse().castToResponse(doctor));
                }
            }

            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getDashboardData() throws InternalServerErrorException {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER));
            response.put("totalDoctors", userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.DOCTOR));
            response.put("unapprovedDoctors", userRepo.countAllByApprovedFalseAndRole(UserRole.DOCTOR));
            response.put("unapprovedDoctorsList", getAllUnapprovedDoctors());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> manageUser(UserStatus status) throws InternalServerErrorException {

        try {

            Map<String, Object> response = new HashMap<>();
            response.put("activeUsers", userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER));
            response.put("suspendedUsers", userRepo.countAllByStatusAndRole(UserStatus.SUSPENDED, UserRole.USER));
            response.put("trashUsers", userRepo.countAllByStatusAndRole(UserStatus.DELETED, UserRole.USER));
            response.put("userList", getUsersByStatus(status));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> manageDoctor(UserStatus status) throws InternalServerErrorException {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("activeDoctor", userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.DOCTOR));
            response.put("suspendedDoctor", userRepo.countAllByStatusAndRole(UserStatus.SUSPENDED, UserRole.DOCTOR));
            response.put("trashDoctor", userRepo.countAllByStatusAndRole(UserStatus.DELETED, UserRole.DOCTOR));
            response.put("doctorsList", getDoctorsByStatus(status));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getAllDataRemovalRequests() throws InternalServerErrorException {
        try {
            List<RemovalRequestResponse> responseRequests = new ArrayList<>();
            for (DataRemovalRequest request : dataRemovalRequestRepo.findAll()) {
                responseRequests.add(new RemovalRequestResponse().castToResponse(request));
            }
            responseRequests.sort(Comparator.comparing(RemovalRequestResponse::getCreatedAt).reversed());
            Map<String, Object> response = new HashMap<>();
            response.put("totalRequest", responseRequests.size());
            response.put("pendingRequest", dataRemovalRequestRepo.countAllByAcceptedIsFalseAndRejectedIsFalse());
            response.put("request", responseRequests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> acceptDataRemovalRequest(UUID requestId)
            throws BadRequestException, InternalServerErrorException {
        try {
            DataRemovalRequest request = dataRemovalRequestRepo.findById(requestId)
                    .orElseThrow(() -> new BadRequestException("Request not found"));
            switch (request.getType()) {
                case "ACCOUNT_DELETION" -> userRepo.delete(request.getUser());
                case "MEDICAL_RECORDS_DELETION" -> {
                    List<MedicalRecords> records = medicalRecordRepo.findAllByUser(request.getUser());
                    medicalRecordRepo.deleteAll(records);
                }
                case "PRESCRIPTION_DELETION" -> {
                    List<Prescriptions> prescriptions = prescriptionRepo.findAllByUser(request.getUser());
                    prescriptionRepo.deleteAll(prescriptions);
                }
                default -> throw new BadRequestException("Invalid removal type");
            }

            request.setAccepted(true);
            dataRemovalRequestRepo.save(request);
            return ResponseEntity.ok(new SuccessResponse("Data removal accepted successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> rejectDataRemovalRequest(UUID requestId)
            throws BadRequestException, InternalServerErrorException {
        try {
            DataRemovalRequest request = dataRemovalRequestRepo.findById(requestId)
                    .orElseThrow(() -> new BadRequestException("Request not found"));
            request.setRejected(true);
            dataRemovalRequestRepo.save(request);
            return ResponseEntity.ok(new SuccessResponse("Data removal rejected successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());

        }
    }
}
