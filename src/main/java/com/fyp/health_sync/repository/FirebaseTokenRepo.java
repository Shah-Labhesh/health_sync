package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FirebaseTokenRepo extends JpaRepository<FirebaseToken, UUID> {
    FirebaseToken findByTokenAndUserIsNotNull(String token);
    List<FirebaseToken> findAllByUser(Users user);
}
