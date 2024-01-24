package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialityRepo extends JpaRepository<Speciality, UUID> {

    List<Speciality> findAllByDeletedAtIsNull();

}
