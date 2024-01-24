package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.DoctorRepo;
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
    private final DoctorRepo doctorRepo;



    public ResponseEntity<?> updateApprovedStatus(UUID doctorId, Boolean status) throws BadRequestException, InternalServerErrorException {
        try {
            Optional<Doctors> doctor = doctorRepo.findById(doctorId);
            if (doctor.isEmpty()){
                throw  new BadRequestException("Doctor not found");
            }
            if (doctor.get().getAccountStatus() != UserStatus.ACTIVE){
                throw  new BadRequestException("Doctor account is not active");
            }
            if (doctor.get().getApproved() == status){
                throw  new BadRequestException("Status already updated");
            }

            doctor.get().setApproved(status);
            doctorRepo.save(doctor.get());

            return ResponseEntity.ok(new SuccessResponse("Status updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updatePopularStatus(UUID doctorId, Boolean status) throws BadRequestException, InternalServerErrorException {
        try {
            Optional<Doctors> doctor = doctorRepo.findById(doctorId);
            if (doctor.isEmpty()){
                throw  new BadRequestException("Doctor not found");
            }
            if (doctor.get().getAccountStatus() != UserStatus.ACTIVE){
                throw  new BadRequestException("Doctor account is not active");
            }
            if (!doctor.get().getApproved()){
                throw  new BadRequestException("Doctor not approved yet");
            }
            if (doctor.get().isPopular() == status){
                throw  new BadRequestException("Status already updated");
            }
            doctor.get().setPopular(status);
            doctorRepo.save(doctor.get());

            return ResponseEntity.ok(new SuccessResponse("Status updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete user
    public ResponseEntity<?> changeUserStatus(UUID userId, UserStatus status) throws BadRequestException, InternalServerErrorException {
        try{
            Optional<Users> user = userRepo.findById(userId);
            if (user.isEmpty()){
                throw  new BadRequestException("User not found");
            }
            if (user.get().getStatus() == UserStatus.DELETED){
                throw  new BadRequestException("User already deleted");
            }
            if (status != UserStatus.DELETED){
                user.get().setDeletedAt(LocalDateTime.now());
                user.get().setStatus(UserStatus.DELETED);
            }else{
                user.get().setStatus(status);
            }

            userRepo.save(user.get());

            return ResponseEntity.ok(new SuccessResponse("User moved to trash successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


    // delete doctor
    public ResponseEntity<?> changeDoctorStatus(UUID doctorId, UserStatus status) throws BadRequestException, InternalServerErrorException {
        try{
            Optional<Doctors> doctor = doctorRepo.findById(doctorId);
            if (doctor.isEmpty()){
                throw  new BadRequestException("Doctor not found");
            }
            if (doctor.get().getAccountStatus() == UserStatus.DELETED){
                throw  new BadRequestException("Doctor already deleted");
            }
            if (status != UserStatus.DELETED){
                doctor.get().setDeletedAt(LocalDateTime.now());
                doctor.get().setAccountStatus(UserStatus.DELETED);
            }else{
                doctor.get().setAccountStatus(status);
            }

            doctorRepo.save(doctor.get());

            return ResponseEntity.ok(new SuccessResponse("Doctor moved to trash successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete user permanently
    @Transactional
    public ResponseEntity<?> deleteUser(UUID userId) throws BadRequestException, InternalServerErrorException {
        try{
            Optional<Users> user = userRepo.findById(userId);
            if (user.isEmpty()){
                throw  new BadRequestException("User not found");
            }
            if (user.get().getStatus() != UserStatus.DELETED){
                throw  new BadRequestException("User not found in trash");
            }
            userRepo.delete(user.get());

            return ResponseEntity.ok(new SuccessResponse("User deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete doctor permanently
    public ResponseEntity<?> deleteDoctor(UUID doctorId) throws BadRequestException, InternalServerErrorException {
        try{
            Optional<Doctors> doctor = doctorRepo.findById(doctorId);
            if (doctor.isEmpty()){
                throw  new BadRequestException("Doctor not found");
            }
            if (doctor.get().getAccountStatus() != UserStatus.DELETED){
                throw  new BadRequestException("Doctor not found in trash");
            }
            doctorRepo.delete(doctor.get());

            return ResponseEntity.ok(new SuccessResponse("Doctor deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // restore user
    public ResponseEntity<?> restoreUser(UUID userId) throws BadRequestException, InternalServerErrorException {
       try{
           Optional<Users> user = userRepo.findById(userId);
           if (user.isEmpty()){
               throw  new BadRequestException("User not found");
           }
           if (user.get().getStatus() != UserStatus.DELETED){
               throw  new BadRequestException("User not found in trash");
           }
           user.get().setDeletedAt(null);
           user.get().setStatus(UserStatus.ACTIVE);
           userRepo.save(user.get());

           return ResponseEntity.ok(new SuccessResponse("User restored successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }

    }

    // restore doctor
    public ResponseEntity<?> restoreDoctor(UUID doctorId) throws BadRequestException, InternalServerErrorException {
       try{
           Optional<Doctors> doctor = doctorRepo.findById(doctorId);
           if (doctor.isEmpty()){
               throw  new BadRequestException("Doctor not found");
           }
           if (doctor.get().getAccountStatus() != UserStatus.DELETED){
               throw  new BadRequestException("Doctor not found in trash");
           }
           doctor.get().setDeletedAt(null);
           doctor.get().setAccountStatus(UserStatus.ACTIVE);
           doctorRepo.save(doctor.get());

           return ResponseEntity.ok(new SuccessResponse("Doctor restored successfully"));
       } catch (Exception e) {
           throw new InternalServerErrorException(e.getMessage());
       }
    }

    //getAllSoftDeletedUsers


    public List<UserResponse> getUsersByStatus(UserStatus status) throws InternalServerErrorException {
        try {
            List<Users> users = userRepo.findAllByStatus(status);
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
        try{
            List<Doctors> doctors = doctorRepo.findAllByAccountStatus(status);
            List<DoctorResponse> response = new ArrayList<>();
            for (Doctors doctor: doctors) {

                    response.add(new DoctorResponse().castToResponse(doctor));
            }
            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    //getAllSoftDeletedDoctors



//    public ResponseEntity<?> getAllUser() throws InternalServerErrorException {
//       try {
//           List<Users>  users = userRepo.findAll();
//           List<UserResponse> response = new ArrayList<>();
//           for (Users user: users) {
//
//               if (user.getStatus() != UserStatus.DELETED){
//
//                   response.add(new UserResponse().castToResponse(user));
//               }
//           }
//           return ResponseEntity.ok(response);
//       }
//         catch (Exception e){
//              throw new InternalServerErrorException(e.getMessage());
//         }
//    }

//    public ResponseEntity<?> getAllDoctors() throws InternalServerErrorException {
//        try{
//            List<Doctors>  doctors = doctorRepo.findAll();
//            List<DoctorResponse> response = new ArrayList<>();
//
//            for (Doctors doctor: doctors) {
//
//                if (doctor.getAccountStatus() != UserStatus.DELETED){
//
//                    response.add(new DoctorResponse().castToResponse(doctor));
//                }
//            }
//
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            throw new InternalServerErrorException(e.getMessage());
//        }
//    }

    public List<DoctorResponse> getAllUnapprovedDoctors() throws InternalServerErrorException {
        try {
            List<Doctors> doctors = doctorRepo.findAllByApprovedFalse();
            List<DoctorResponse> response = new ArrayList<>();

            for (Doctors doctor : doctors) {

                if (doctor.getAccountStatus() == UserStatus.ACTIVE) {

                    response.add(new DoctorResponse().castToResponse(doctor));
                }
            }

            return response;
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // count of all users and doctors
    public Integer countUsersByStatus(UserStatus status ) throws InternalServerErrorException {
        try{
            return userRepo.countAllByStatus(status);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public Integer countDoctorsByStatus(UserStatus status) throws InternalServerErrorException {
        try{
            return doctorRepo.countAllByAccountStatus(status);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public Integer countAllUnapprovedDoctors() throws InternalServerErrorException {
        try{
            return doctorRepo.countAllByApprovedFalse();
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getDashboardData() throws InternalServerErrorException {
        try{
            return ResponseEntity.ok().body(AdminDashboardResponse
                    .builder()
                    .totalUsers(countUsersByStatus(UserStatus.ACTIVE))
                    .totalDoctors(countDoctorsByStatus(UserStatus.ACTIVE))
                    .totalUnapprovedDoctors(countAllUnapprovedDoctors())
                    .doctors(getAllUnapprovedDoctors())
                    .build());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> manageUser(UserStatus status ) throws BadRequestException, InternalServerErrorException {

        try{

            Integer activeUsers = countUsersByStatus(UserStatus.ACTIVE);
            Integer suspendedUsers = countUsersByStatus(UserStatus.SUSPENDED);
            Integer trashUsers = countUsersByStatus(UserStatus.DELETED);
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
    public ResponseEntity<?> manageDoctor(UserStatus status) throws BadRequestException, InternalServerErrorException {
        try{
//            UserStatus status1 = null;
//            switch (status) {
//                case "ACTIVE" -> status1 = UserStatus.ACTIVE;
//                case "SUSPENDED" -> status1 = UserStatus.SUSPENDED;
//                case "DELETED" -> status1 = UserStatus.DELETED;
//                default -> throw new BadRequestException("Invalid status");
//            }
            Integer activeDoctor = countDoctorsByStatus(UserStatus.ACTIVE);
            Integer suspendedDoctor = countDoctorsByStatus(UserStatus.SUSPENDED);
            Integer trashDoctor = countDoctorsByStatus(UserStatus.DELETED);
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
