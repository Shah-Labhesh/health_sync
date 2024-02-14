package com.fyp.health_sync.repository;


import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRoomRepo extends JpaRepository<ChatRoom, UUID> {

    ChatRoom findByUserIdAndDoctorIdAndDeletedAtNull(UUID user_id, UUID doctor_id);

    List<ChatRoom> findAllByUserOrDoctor(Users users, Users users1);

    ChatRoom findByIdAndAndDeletedAtNull(UUID id);
}
