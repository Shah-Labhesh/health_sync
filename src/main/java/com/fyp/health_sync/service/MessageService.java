package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.MessageType;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.ChatRoomRepo;
import com.fyp.health_sync.repository.MessageRepo;
import com.fyp.health_sync.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatRoomRepo chatRoomRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;


    public Message createMessage(UUID roomId, String message) throws BadRequestException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepo.findByEmail(user);


        if (users == null){
            throw new BadRequestException("User not found");
        }
        if (users.getStatus() != UserStatus.ACTIVE){
            throw new BadRequestException("User is not active");
        }
        ChatRoom chatRoom = chatRoomRepo.findById(roomId).orElseThrow(() -> new BadRequestException("Chat Room not found"));
        UUID user1 = chatRoom.getUser().getId();
        UUID user2 = chatRoom.getDoctor().getId();
        Message newMessage = Message.builder()
                .chatRoom(chatRoom)
                .createdAt(LocalDateTime.now())
                .messageType(MessageType.TEXT)
                .message(message)
                .senderId(user1 == users.getId() ? user1 : user2)
                .receiverId(user1 == users.getId() ? user2 : user1)
                .build();
        return messageRepo.save(newMessage);


    }



    public ResponseEntity<?> getRoomMessages(UUID roomId) throws BadRequestException {
        return ResponseEntity.ok(messageRepo.findAllByChatRoomId(roomId));
    }

}
