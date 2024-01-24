package com.fyp.health_sync.repository;


import com.fyp.health_sync.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepo extends JpaRepository<Message, UUID>{
    List<Message> findAllByChatRoomId(UUID roomId);
}
