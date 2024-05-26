package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.ShareMedicalRecords;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShareRecordRepo extends JpaRepository<ShareMedicalRecords, UUID> {

    List<ShareMedicalRecords> findByUserAndDoctorIsNotNull(Users user);

    List<ShareMedicalRecords> findByDoctor(Users doctor);

//    Optional<ShareMedicalRecords> findByMedicalRecordsAndDoctor(MedicalRecords medicalRecords, Users doctor);

    @Query("SELECT s FROM ShareMedicalRecords s WHERE s.doctor = ?1 AND s.user = ?2 AND s.isAccepted = ?3 AND s.isExpired = ?4")
    ShareMedicalRecords findByDoctorAndUserAndAcceptedAndExpired(Users doctor, Users user, boolean accepted, boolean expired);

    List<ShareMedicalRecords> findByDoctorAndUser(Users doctor, Users user);
}
