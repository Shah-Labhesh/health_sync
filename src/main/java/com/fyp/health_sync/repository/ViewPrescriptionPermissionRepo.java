package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.entity.ViewPrescriptionPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ViewPrescriptionPermissionRepo extends JpaRepository<ViewPrescriptionPermission, UUID> {


    List<ViewPrescriptionPermission> findByDoctorAndUser(Users doctor, Users user);

    List<ViewPrescriptionPermission> findAllByDoctor(Users doctor);

    List<ViewPrescriptionPermission> findAllByUser(Users user);

    @Query("SELECT v FROM ViewPrescriptionPermission v WHERE v.doctor = ?1 AND v.user = ?2 AND v.isAccepted = ?3 AND v.isExpired = ?4")
    ViewPrescriptionPermission findByDoctorAndUserAndAcceptedAndExpired(Users doctor, Users user, boolean b,
            boolean c);
}
