package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Notification;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByReceiver(Users user);

    @Query("SELECT COUNT(e) FROM Notification e WHERE e.receiver = :user AND e.isRead = :isRead")
    Integer countAllByUserAndRead(Users user, Boolean isRead);

    @Query("SELECT e FROM Notification e WHERE e.receiver = :user AND e.isRead = :b")
    List<Notification> findAllByReceiverAndRead(Users user, Boolean b);
}
