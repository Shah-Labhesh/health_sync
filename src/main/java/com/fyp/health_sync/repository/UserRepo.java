package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<Users, UUID> {

    Users findByEmail(String email);

    Optional<Users> findById(UUID id);

    @Query("SELECT u FROM Users u WHERE " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(u.latitude)))) < :radius " +
            "AND u.role = com.fyp.health_sync.enums.UserRole.DOCTOR AND u.approved = true AND u.status = com.fyp.health_sync.enums.UserStatus.ACTIVE")
    List<Users> findNearbyDoctors(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radius") double radius);

    List<Users> findAllByIdAndRole(UUID id, UserRole role);
    List<Users> findAllByStatusAndRole(UserStatus userStatus, UserRole role);

    List<Users> findAllByStatusAndRoleAndApproved(UserStatus userStatus, UserRole role, Boolean approved);
    List<Users> findAllByStatus(UserStatus userStatus);

    Integer countAllByStatus(UserStatus userStatus);
    Integer countAllByStatusAndRole(UserStatus userStatus, UserRole role);

    List<Users> findAllByApprovedFalseAndRole(UserRole userRole);

    Integer countAllByApprovedFalseAndRole(UserRole userRole);

//    Slice<Users> findAllByRole(UserRole userRole, Pageable p);
}
