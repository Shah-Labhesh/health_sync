package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.entity.ViewPrescriptionPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ViewPrescriptionPermissionRepo extends JpaRepository<ViewPrescriptionPermission, UUID> {


    ViewPrescriptionPermission findByDoctorAndUser(Users doctor, Users user);

    List<ViewPrescriptionPermission> findAllByDoctor(Users doctor);

    List<ViewPrescriptionPermission> findAllByUser(Users user);
}
