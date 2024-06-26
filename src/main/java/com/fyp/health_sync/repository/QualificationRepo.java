package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Qualifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualificationRepo extends JpaRepository<Qualifications, UUID> {

    List<Qualifications> findByDoctorIdId(UUID doctorId);


    void deleteQualificationsById(UUID id);


}
