//package com.fyp.health_sync.repository;
//
//import com.fyp.health_sync.entity.Doctors;
//import com.fyp.health_sync.enums.UserStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface DoctorRepo extends JpaRepository<Doctors, UUID> {
//
//    Doctors findByEmail(String email);
//
//    Optional<Doctors> findById(UUID id);
//
//    List<Doctors> findAllByAccountStatus(UserStatus userStatus);
//
//    List<Doctors> findAllByApprovedFalse();
//
//    Integer countAllByAccountStatus(UserStatus userStatus);
//
//    Integer countAllByApprovedFalse();
//}
