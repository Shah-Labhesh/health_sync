package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Prescriptions;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepo extends JpaRepository<Prescriptions, UUID> {

    List<Prescriptions> findByUser(Users user);

    List<Prescriptions> findByDoctor(Users user);
}
