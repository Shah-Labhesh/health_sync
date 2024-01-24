package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.MessageType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.ChatRoomRepo;
import com.fyp.health_sync.repository.MessageRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatRoomRepo chatRoomRepo;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;
    private final MessageRepo messageRepo;


    public Message createMessage(UUID roomId, String message) throws BadRequestException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepo.findByEmail(user);
        Doctors doctors = doctorRepo.findByEmail(user);
        Optional<ChatRoom> chatRoom = chatRoomRepo.findById(roomId);


        if (chatRoom.isPresent()){
            Message newMessage = Message.builder()
                    .chatRoom(chatRoom.get())
                    .createdAt(LocalDateTime.now())
                    .messageType(MessageType.TEXT)
                    .message(message)
                    .senderId(users != null ? users.getId() : doctors.getId())
                    .receiverId(users != null ? doctors.getId() : users.getId())
                    .build();
            return messageRepo.save(newMessage);
        }else {
            throw new BadRequestException("Room not found");
        }


    }

    public ResponseEntity<?> getRoomMessages(UUID roomId) throws BadRequestException {
        return ResponseEntity.ok(messageRepo.findAllByChatRoomId(roomId));
    }

}
