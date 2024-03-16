package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Payment;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByDoctor(Users user);
    List<Payment> findAllByUser(Users user);

    @Query("SELECT SUM(p.amount) FROM Payment p where p.transactionId is not null")
    Integer countTotalAmount();

    @Query("SELECT SUM(amount) FROM Payment where transactionId is null")
    Integer countPendingAmount();

    Payment findByAppointment(Appointments appointment);
}
