package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.UploadPrescriptionDto;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Prescriptions;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.entity.ViewPrescriptionPermission;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.PrescriptionRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.repository.ViewPrescriptionPermissionRepo;
import com.fyp.health_sync.utils.ImageUtils;
import com.fyp.health_sync.utils.PrescriptionPermissionResponse;
import com.fyp.health_sync.utils.PrescriptionResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final ViewPrescriptionPermissionRepo viewPPRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final FirebaseTokenRepo firebaseTokenRepo;

    public ResponseEntity<?> savePrescription(UploadPrescriptionDto prescription)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to upload prescription");
            }
            Users user = userRepo.findById(prescription.getUserId())
                    .orElseThrow(() -> new BadRequestException("User not found"));
            if (user.getRole() != UserRole.USER) {
                throw new BadRequestException("User is not a patient");
            }
            if (prescription.getRecordType().equals(RecordType.IMAGE.name())
                    || prescription.getRecordType().equals(RecordType.PDF.name())) {
                if (prescription.getPrescription() == null) {
                    throw new BadRequestException("Prescription must not be empty");
                }
            } else if (prescription.getRecordType().equals(RecordType.TEXT.name())) {
                if (prescription.getPrescriptionText() == null) {
                    throw new BadRequestException("Prescription text must not be empty");
                }
            }
            Prescriptions prescriptions = prescriptionRepo.save(Prescriptions.builder()
                    .recordType(prescription.getRecordType())
                    .prescription(prescription.getPrescription() != null
                            ? ImageUtils.compress(prescription.getPrescription().getBytes())
                            : null)
                    .prescriptionText(prescription.getPrescriptionText())
                    .createdAt(LocalDateTime.now())
                    .doctor(doctor)
                    .user(user)
                    .build());
            notificationService.sendNotification(prescriptions.getId(),
                    "You have a new prescription from Dr. " + prescriptions.getDoctor().getName(),
                    NotificationType.PRESCRIPTION, prescriptions.getUser().getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(user)) {
                pushNotificationService.sendNotification("New Prescription",
                        "You have a new prescription from Dr. " + prescriptions.getDoctor().getName(),
                        token.getToken());
            }
            return ResponseEntity.created(null).body(new PrescriptionResponse().castToResponse(prescriptions));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }

    }

    public ResponseEntity<?> getMyPrescriptions() throws BadRequestException, InternalServerErrorException {
        try {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            List<PrescriptionResponse> prescriptionResponses = new ArrayList<>();
            if (user.getRole() == UserRole.DOCTOR) {
                for (Prescriptions prescription : prescriptionRepo.findByDoctor(user)) {
                    prescriptionResponses.add(new PrescriptionResponse().castToResponse(prescription));
                }
            } else {
                for (Prescriptions prescription : prescriptionRepo.findByUser(user)) {
                    prescriptionResponses.add(new PrescriptionResponse().castToResponse(prescription));
                }
            }
            return ResponseEntity.ok(prescriptionResponses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // request for prescription of user by doctor
    public ResponseEntity<?> requestPrescription(UUID userId)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to request prescription");
            }
            Users user = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
            List<ViewPrescriptionPermission> permissions = viewPPRepo.findByDoctorAndUser(doctor, user);
            if (permissions != null) {
                for (ViewPrescriptionPermission viewPrescriptionPermission : permissions) {
                    if (!viewPrescriptionPermission.isExpired() && !viewPrescriptionPermission.isRejected()) {
                        throw new BadRequestException("Request already sent");
                    }
                    if (viewPrescriptionPermission.isAccepted() && !viewPrescriptionPermission.isExpired()) {

                        throw new BadRequestException("Request already accepted");
                    }
                }
            }
            ViewPrescriptionPermission newPermission = ViewPrescriptionPermission.builder()
                    .isAccepted(false)
                    .isRejected(false)
                    .isExpired(false)
                    .doctor(doctor)
                    .user(user)
                    .build();

            viewPPRepo.save(newPermission);
            notificationService.sendNotification(newPermission.getId(),
                    "Dr. " + doctor.getName() + " has requested to view your prescription",
                    NotificationType.PRESCRIPTION_PERMISSION, newPermission.getUser().getId());
            for (FirebaseToken token : firebaseTokenRepo.findAllByUser(user)) {
                pushNotificationService.sendNotification("Prescription Request",
                        "Dr. " + doctor.getName() + " has requested to view your prescription", token.getToken());
            }

            return ResponseEntity.ok(new SuccessResponse("View Prescription Requested successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getPrescriptionsOfUser(UUID userId)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to view prescriptions");
            }
            Users user = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
            ViewPrescriptionPermission permission = viewPPRepo.findByDoctorAndUserAndAcceptedAndExpired(doctor,
                    user, true, false);
            if (permission == null) {
                throw new BadRequestException("You don't have permission to view prescription");
            }
            if (permission.isRejected() && !permission.isExpired()) {
                throw new BadRequestException("Permission to view prescription is rejected");
            }
            if (!permission.isAccepted() && !permission.isExpired()) {
                throw new BadRequestException("Permission to view prescription is not accepted yet");
            }

            List<PrescriptionResponse> prescriptionResponses = new ArrayList<>();
            for (Prescriptions prescription : prescriptionRepo.findAllByUser(user)) {
                prescriptionResponses.add(new PrescriptionResponse().castToResponse(prescription));
            }
            return ResponseEntity.ok(prescriptionResponses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // accept or reject request for prescription of user
    public ResponseEntity<?> acceptOrRejectRequest(UUID permissionId, boolean isAccepted)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("You are not authorized to accept or reject request");
            }
            ViewPrescriptionPermission permission = viewPPRepo.findById(permissionId)
                    .orElseThrow(() -> new BadRequestException("Permission not found"));
            if (!permission.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You are not authorized to accept or reject this request");
            }
            if (permission.isAccepted() || permission.isRejected()) {
                throw new BadRequestException("Request already " + (permission.isAccepted() ? "accepted" : "rejected"));
            }
            if (permission.isExpired()) {
                throw new BadRequestException("Request is expired");
            }
            if (isAccepted) {
                permission.setRejected(false);
                permission.setAccepted(true);
            } else {
                permission.setRejected(true);
                permission.setAccepted(false);
            }
            viewPPRepo.save(permission);
            if (isAccepted) {
                notificationService.sendNotification(permission.getId(),
                        user.getName() + " has accepted your request to view prescription",
                        NotificationType.PRESCRIPTION_PERMISSION, permission.getDoctor().getId());
                for (FirebaseToken token : firebaseTokenRepo.findAllByUser(permission.getUser())) {
                    pushNotificationService.sendNotification("Prescription Request",
                            user.getName() + " has accepted your request to view prescription", token.getToken());
                }
            } else {
                notificationService.sendNotification(permission.getId(),
                        user.getName() + " has rejected your request to view prescription",
                        NotificationType.PRESCRIPTION_PERMISSION, permission.getDoctor().getId());
                for (FirebaseToken token : firebaseTokenRepo.findAllByUser(permission.getUser())) {
                    pushNotificationService.sendNotification("Prescription Request",
                            user.getName() + " has rejected your request to view prescription", token.getToken());
                }
            }
            return ResponseEntity.created(null)
                    .body(new SuccessResponse("Request " + (isAccepted ? "accepted" : "rejected") + " successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // get all request permission
    public ResponseEntity<?> getAllRequestPermission()
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() == UserRole.DOCTOR) {
                List<PrescriptionPermissionResponse> permissions = new ArrayList<>();
                for (ViewPrescriptionPermission permission : viewPPRepo.findAllByDoctor(user)) {
                    permissions.add(new PrescriptionPermissionResponse().castToResponse(permission));
                }
                ;
                return ResponseEntity.ok(permissions);
            } else if (user.getRole() == UserRole.USER) {

                List<PrescriptionPermissionResponse> permissions = new ArrayList<>();
                for (ViewPrescriptionPermission permission : viewPPRepo.findAllByUser(user)) {
                    permissions.add(new PrescriptionPermissionResponse().castToResponse(permission));
                }
                ;
                return ResponseEntity.ok(permissions);
            } else {
                throw new ForbiddenException("You are not authorized to view request permission");
            }

        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> revokePermission(UUID permissionId)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("You are not authorized to revoke permission");
            }
            ViewPrescriptionPermission permission = viewPPRepo.findById(permissionId)
                    .orElseThrow(() -> new BadRequestException("Permission not found"));
            if (!permission.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You are not authorized to revoke this permission");
            }
            if (permission.isRejected()) {
                throw new BadRequestException("You can't revoke rejected permission");
            }
            permission.setExpired(true);
            viewPPRepo.save(permission);
            return ResponseEntity.ok(new SuccessResponse("Permission revoked successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> cancelPermissionRequest(UUID permissionId)
            throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users doctor = userRepo.findByEmail(email);
            if (doctor == null) {
                throw new BadRequestException("Doctor not found");
            }
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new ForbiddenException("You are not authorized to cancel permission request");
            }
            ViewPrescriptionPermission permission = viewPPRepo.findById(permissionId)
                    .orElseThrow(() -> new BadRequestException("Permission not found"));
            if (!permission.getDoctor().getId().equals(doctor.getId())) {
                throw new ForbiddenException("You are not authorized to cancel this permission request");
            }
            if (permission.isRejected()) {
                throw new BadRequestException("You can't cancel rejected permission request");
            }
            viewPPRepo.delete(permission);
            return ResponseEntity.ok(new SuccessResponse("Permission request cancelled successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
