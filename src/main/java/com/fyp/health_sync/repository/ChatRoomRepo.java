package com.fyp.health_sync.repository;


import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRoomRepo extends JpaRepository<ChatRoom, UUID> {

    List<ChatRoom> findAllByUserId(Users userId);

    List<ChatRoom> findAllByDoctorId(UUID doctorId);

    ChatRoom findByUserIdAndDoctorId(Users userId, UUID doctorId);
}
