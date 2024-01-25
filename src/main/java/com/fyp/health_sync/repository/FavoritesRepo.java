package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Favorites;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FavoritesRepo extends JpaRepository<Favorites, UUID> {

    Favorites findByDoctorAndUser(Users doctor, Users user);
}
