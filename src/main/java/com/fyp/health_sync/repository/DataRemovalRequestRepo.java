package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.DataRemovalRequest;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

@Repository
public interface DataRemovalRequestRepo extends JpaRepository<DataRemovalRequest, UUID>{

    List<DataRemovalRequest> findAllByUserAndType(Users user, String type);

    List<DataRemovalRequest> findAllByUser(Users user);

    @Query("SELECT COUNT(d) FROM DataRemovalRequest d WHERE d.isAccepted = false AND d.isRejected = false")
    Integer countAllByAcceptedIsFalseAndRejectedIsFalse();
}
