package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.MedicalRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepo extends JpaRepository<MedicalRecords, UUID> {

    List<MedicalRecords> findByUserIdAndDeletedAtNotNull(UUID userId);
}
