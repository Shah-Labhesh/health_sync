package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<Users, UUID> {

    Users findByEmail(String email);

    Optional<Users> findById(UUID id);


    List<Users> findAllByStatus(UserStatus userStatus);

    Integer countAllByStatus(UserStatus userStatus);

//    void deleteById(UUID id);


}
