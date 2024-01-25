package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.AdminDashboardResponse;
import com.fyp.health_sync.utils.DoctorResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import com.fyp.health_sync.utils.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepo userRepo;
//    private final DoctorRepo doctorRepo;



    public ResponseEntity<?> updateApprovedStatus(UUID id, Boolean status) throws BadRequestException, InternalServerErrorException {
            Users doctor = userRepo.findById(id).orElseThrow( () -> new BadRequestException("Doctor not found"));

            if (doctor.getStatus() != UserStatus.ACTIVE){
                throw  new BadRequestException("Doctor account is not active");
            }
            if (doctor.getApproved() == status){
                throw  new BadRequestException("Status already updated");
            }
        try {

            doctor.setApproved(status);
            userRepo.save(doctor);

            return ResponseEntity.ok(new SuccessResponse("Approval Status updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updatePopularStatus(UUID id, Boolean status) throws BadRequestException, InternalServerErrorException {
            Users doctor = userRepo.findById(id).orElseThrow( () -> new BadRequestException("Doctor not found"));

            if (doctor.getStatus() != UserStatus.ACTIVE){
                throw  new BadRequestException("Doctor account is not active");
            }
            if (!doctor.getApproved()){
                throw  new BadRequestException("Doctor not approved yet");
            }
            if (doctor.isPopular() == status){
                throw  new BadRequestException("Status already updated");
            }
        try {

            doctor.setPopular(status);
            userRepo.save(doctor);

            return ResponseEntity.ok(new SuccessResponse("Popular Status updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete user
    public ResponseEntity<?> changeAccountStatus(UUID id, UserStatus status) throws BadRequestException, InternalServerErrorException {
            Users user = userRepo.findById(id).orElseThrow( () -> new BadRequestException("User not found"));

            if (user.getStatus() == UserStatus.DELETED){
                throw  new BadRequestException("User already deleted");
            }
            if (status != UserStatus.DELETED){
                user.setDeletedAt(LocalDateTime.now());
                user.setStatus(UserStatus.DELETED);
            }else{
                user.setStatus(status);
            }
        try{

            userRepo.save(user);

            return ResponseEntity.ok(new SuccessResponse("Account status updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


    // delete user permanently
    @Transactional
    public ResponseEntity<?> deleteUser(UUID userId) throws BadRequestException, InternalServerErrorException {
            Users user = userRepo.findById(userId).orElseThrow( () -> new BadRequestException("User not found"));

            if (user.getStatus() != UserStatus.DELETED){
                throw  new BadRequestException("User not found in trash");
            }
        try{

            userRepo.delete(user);

            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // restore user
    public ResponseEntity<?> restoreUser(UUID userId) throws BadRequestException, InternalServerErrorException {
           Users user = userRepo.findById(userId).orElseThrow( () -> new BadRequestException("User not found"));

           if (user.getStatus() != UserStatus.DELETED){
               throw  new BadRequestException("User not found in trash");
           }

        try{

           user.setDeletedAt(null);
           user.setStatus(UserStatus.ACTIVE);
           userRepo.save(user);

           return ResponseEntity.ok(new SuccessResponse("User restored successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }

    }

    //getAllSoftDeletedUsers
    private List<UserResponse> getUsersByStatus(UserStatus status) throws InternalServerErrorException {
        try {
            List<Users> users = userRepo.findAllByStatusAndRole(status,UserRole.USER);
            List<UserResponse> response = new ArrayList<>();
            for (Users user: users) {
                response.add(new UserResponse().castToResponse(user));
            }

            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public List<DoctorResponse> getDoctorsByStatus(UserStatus status) throws InternalServerErrorException {
        try {
            List<Users> users = userRepo.findAllByStatusAndRole(status,UserRole.DOCTOR);
            List<DoctorResponse> response = new ArrayList<>();
            for (Users user: users) {
                response.add(new DoctorResponse().castToResponse(user));
            }
            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    //getAllSoftDeletedDoctors




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

    // count of all users and doctors
    public Integer countUsersByStatus(UserStatus status, UserRole role ) throws InternalServerErrorException {
        try{
            if (role == UserRole.USER){
                return userRepo.countAllByStatusAndRole(status,role);
            }else if (role == UserRole.DOCTOR) {
                return userRepo.countAllByStatus(status);
            }
            return 0;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }



    public Integer countAllUnapprovedDoctors() throws InternalServerErrorException {
        try{
            return userRepo.countAllByApprovedFalseAndRole(UserRole.DOCTOR);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getDashboardData() throws InternalServerErrorException {
        try{
            return ResponseEntity.ok().body(AdminDashboardResponse
                    .builder()
                    .totalUsers(countUsersByStatus(UserStatus.ACTIVE, UserRole.USER))
                    .totalDoctors(countUsersByStatus(UserStatus.ACTIVE, UserRole.DOCTOR))
                    .totalUnapprovedDoctors(countAllUnapprovedDoctors())
                    .doctors(getAllUnapprovedDoctors())
                    .build());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> manageUser(UserStatus status) throws  InternalServerErrorException {

        try{

            Integer activeUsers = countUsersByStatus(UserStatus.ACTIVE, UserRole.USER);
            Integer suspendedUsers = countUsersByStatus(UserStatus.SUSPENDED, UserRole.USER);
            Integer trashUsers = countUsersByStatus(UserStatus.DELETED, UserRole.USER);
            List<UserResponse> userList = getUsersByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("activeUsers", activeUsers);
            response.put("suspendedUsers", suspendedUsers);
            response.put("trashUsers", trashUsers);
            response.put("userList", userList);
            return ResponseEntity.ok(response);
        }catch (Exception e){
           throw new InternalServerErrorException(e.getMessage());
        }
    }
    public ResponseEntity<?> manageDoctor(UserStatus status) throws InternalServerErrorException {
        try{

            Integer activeDoctor = countUsersByStatus(UserStatus.ACTIVE, UserRole.DOCTOR);
            Integer suspendedDoctor = countUsersByStatus(UserStatus.SUSPENDED, UserRole.DOCTOR);
            Integer trashDoctor = countUsersByStatus(UserStatus.DELETED, UserRole.DOCTOR);
            List<DoctorResponse> doctorsList = getDoctorsByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("activeDoctor", activeDoctor);
            response.put("suspendedDoctor", suspendedDoctor);
            response.put("trashDoctor", trashDoctor);
            response.put("doctorsList", doctorsList);
            return ResponseEntity.ok(response);
        }catch (Exception e){
           throw new InternalServerErrorException(e.getMessage());
        }
    }
}
