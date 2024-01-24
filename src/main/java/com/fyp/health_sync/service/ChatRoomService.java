package com.fyp.health_sync.service;


import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.ChatRoomRepo;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepo chatRoomRepo;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;


    public ResponseEntity<?> createChatRoom(UUID doctorId) throws BadRequestException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepo.findByEmail(user);

        if (users != null) {
            ChatRoom chatRoom = chatRoomRepo.findByUserIdAndDoctorId(users, doctorId);
            if (chatRoom != null) {
                return ResponseEntity.ok(chatRoom);
            }
            ChatRoom newChatRoom = ChatRoom.builder()
                    .doctor(doctorRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found")))
                    .user(users)
                    .createdAt(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(chatRoomRepo.save(newChatRoom ));

        }  else {
            throw new BadRequestException("User not found");

        }
    }


    public ResponseEntity<?> getChatRoom(UUID roomId) throws BadRequestException {
        return ResponseEntity.ok(chatRoomRepo.findById(roomId).orElseThrow(() -> new BadRequestException("Room not found")));
    }


    public ResponseEntity<?> getMyChatRooms() throws BadRequestException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        Users users = userRepo.findByEmail(user);
        Doctors doctors = doctorRepo.findByEmail(user);
        if (users != null) {
            return ResponseEntity.ok(chatRoomRepo.findAllByUserId(users));

        } else if (doctors != null) {
            return ResponseEntity.ok(chatRoomRepo.findAllByDoctorId(doctors.getId()));

        } else {
            throw  new BadRequestException("User not found");

        }


    }
}
