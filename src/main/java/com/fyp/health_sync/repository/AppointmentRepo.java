package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointments, UUID> {



    List<Appointments> findAllByDoctor(Users doctor);

   List<Appointments> findAllByUser(Users user);

    List<Appointments> findAllByIsExpiredFalseAndSlot_EndTimeIsBeforeAndDoctorIsNotNull(LocalDateTime now);

    List<Appointments> findAllByIsExpiredFalseAndReminderTimeIsBeforeAndDoctorIsNotNull(LocalDateTime now);

    List<Appointments> findAllByIsExpiredFalseAndSlot_SlotDateTimeIsBeforeAndDoctorIsNotNull(LocalDateTime localDateTime);
}
