package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Slots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlotRepo extends JpaRepository<Slots, UUID> {

    Slots findBySlotId(UUID slotId);

    List<Slots> findAllByDoctorId(UUID id);

    List<Slots> findByDoctorIdAndIsBookedIsFalseAndSlotDateTimeIsGreaterThanEqual(UUID doctorId, LocalDateTime dateTime);

    Slots findBySlotDateTimeAndDoctor(LocalDateTime slotDateTime, Doctors doctor);

    List<Slots> findAllByDoctorIdOrderBySlotDateTime(UUID id);
}
