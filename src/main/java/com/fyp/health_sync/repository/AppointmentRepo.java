package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointments, UUID> {



    List<Appointments> findAllByDoctor(Users doctor);

   List<Appointments> findAllByUser(Users user);

}
