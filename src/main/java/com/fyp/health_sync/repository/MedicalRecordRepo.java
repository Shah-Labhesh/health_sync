package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepo extends JpaRepository<MedicalRecords, UUID> {


    List<MedicalRecords> findByUserAndDeletedAtNull(Users user);

    List<MedicalRecords> findByDoctorAndDeletedAtNull(Users doctor);

    List<MedicalRecords> findAllByUser(Users user);
}
