package com.fyp.health_sync.service;


import com.fyp.health_sync.config.JwtHelper;
import com.fyp.health_sync.entity.ChatRoom;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Message;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.MessageType;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.ChatRoomRepo;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.MessageRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ChatRoomResponse;
import com.fyp.health_sync.utils.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatRoomRepo chatRoomRepo;
    private final UserRepo userRepo;
    private final MessageRepo messageRepo;
    private final JwtHelper jwtHelper;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final FirebaseTokenRepo firebaseTokenRepo;


    public ResponseEntity<?> createChatRoom(UUID doctorId) throws BadRequestException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = userRepo.findByEmail(email);

        if (users != null) {
            ChatRoom chatRoom = chatRoomRepo.findByUserIdAndDoctorIdAndDeletedAtNull(users.getId(), doctorId);
            if (chatRoom != null) {
                return ResponseEntity.ok(new ChatRoomResponse().castToResponse(chatRoom));
            }
            ChatRoom newChatRoom = ChatRoom.builder()
                    .doctor(userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found")))
                    .user(users)
                    .createdAt(LocalDateTime.now())
                    .build();
            chatRoomRepo.save(newChatRoom);
            return ResponseEntity.ok(new ChatRoomResponse().castToResponse(newChatRoom));

        } else {
            throw new BadRequestException("User not found");

        }
    }


    public MessageResponse createMessage(UUID roomId, String message, String token) throws BadRequestException, InternalServerErrorException {
        String email = jwtHelper.getUsernameFromToken(token);
        Users users = userRepo.findByEmail(email);
        if (users == null) {
            throw new BadRequestException("User not found");
        }
        if (users.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User is not active");
        }
        ChatRoom chatRoom = chatRoomRepo.findById(roomId).orElseThrow(() -> new BadRequestException("Chat Room not found"));
        Users receiver = chatRoom.getUser().getId().equals(users.getId()) ? chatRoom.getDoctor() : chatRoom.getUser();
        Message newMessage = Message.builder()
                .chatRoom(chatRoom)
                .createdAt(LocalDateTime.now())
                .messageType(MessageType.TEXT)
                .message(message)
                .senderId(users.getId())
                .receiverId(receiver.getId())
                .build();
        chatRoom.setLastMessage(message);
        chatRoom.setLastMessageAt(LocalDateTime.now());
        chatRoom.setMessageType(MessageType.TEXT);
        chatRoom.setSenderId(users.getId());
        chatRoomRepo.save(chatRoom);
        messageRepo.save(newMessage);
        MessageResponse mes = new MessageResponse().castToResponse(newMessage);
        mes.setMe(newMessage.getSenderId().equals(users.getId()));

        if (receiver.isTextNotification()){
        notificationService.sendNotification(mes.getChatRoom().getId(), "You have a new message from " + users.getName(), NotificationType.CHAT,receiver.getId());
            for (FirebaseToken t : firebaseTokenRepo.findAllByUser(receiver)) {
                 pushNotificationService.sendNotification("New Message", "You have a new message from " + users.getName(), t.getToken());
            }
        }
        return mes;

    }

    public ResponseEntity<?> getMyChatRooms() throws BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users users = userRepo.findByEmail(email);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            List<ChatRoomResponse> response = new ArrayList<>();
            for (ChatRoom chatRoom :
                    chatRoomRepo.findAllByUserOrDoctor(users, users)) {
                response.add(new ChatRoomResponse().castToResponse(chatRoom));
            }
            response.sort((o1, o2) -> o2.getLastMessageAt().compareTo(o1.getLastMessageAt()));
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


    // get chat room by id and deletedAt null
    public ResponseEntity<?> getChatRoom(UUID roomId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();
            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            ChatRoom chatRoom = chatRoomRepo.findByIdAndAndDeletedAtNull(roomId);
            if (chatRoom == null) {
                throw new BadRequestException("Chat Room already deleted");
            }
            if (chatRoom.getUser().getId() != users.getId() || chatRoom.getDoctor().getId() != users.getId()) {
                throw new ForbiddenException("You are not authorized to view this chat room");
            }
            return ResponseEntity.ok(new ChatRoomResponse().castToResponse(chatRoom));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // delete my chat room
    public ResponseEntity<?> deleteMyChatRoom(UUID roomId) throws BadRequestException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();
            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            ChatRoom chatRoom = chatRoomRepo.findById(roomId).orElseThrow(() -> new BadRequestException("Chat Room not found"));
            if (chatRoom.getUser().getId() != users.getId() && chatRoom.getDoctor().getId() != users.getId()) {
                throw new BadRequestException("Chat Room not found");
            }

            if (chatRoom.getDeletedAt() != null) {
                throw new BadRequestException("Chat Room already deleted");
            }
            chatRoom.setDeletedBy(users.getId() == chatRoom.getUser().getId() ? UserRole.USER : UserRole.DOCTOR);
            chatRoom.setDeletedAt(LocalDateTime.now());
            chatRoomRepo.save(chatRoom);
            return ResponseEntity.ok("Chat Room deleted successfully");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getRoomMessages(UUID roomId, String token) throws BadRequestException, ForbiddenException {
        String user = jwtHelper.getUsernameFromToken(token);
        Users users = userRepo.findByEmail(user);
        if (users == null) {
            throw new BadRequestException("User not found");
        }
        ChatRoom chatRoom = chatRoomRepo.findByIdAndAndDeletedAtNull(roomId);
        if (chatRoom == null) {
            throw new BadRequestException("Chat Room already deleted");
        }
        List<MessageResponse> response = new ArrayList<>();

        if (users.getRole() == UserRole.USER && chatRoom.getUser().getId().equals(users.getId())) {
            for (Message message : messageRepo.findAllByChatRoomId(roomId)) {
                MessageResponse mes = new MessageResponse().castToResponse(message);
                mes.setMe(message.getSenderId().equals(users.getId()));
                response.add(mes);
            }
        } else if (users.getRole() == UserRole.DOCTOR && chatRoom.getDoctor().getId().equals(users.getId())) {
            for (Message message : messageRepo.findAllByChatRoomId(roomId)) {
                MessageResponse mes = new MessageResponse().castToResponse(message);
                mes.setMe(message.getSenderId().equals(users.getId()));
                response.add(mes);
            }
        } else {
            throw new ForbiddenException("You are not authorized to view this chat room");
        }
        return ResponseEntity.ok(response);
    }

    // delete message
    public ResponseEntity<?> deleteMessage(UUID messageId) throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            String user = SecurityContextHolder.getContext().getAuthentication().getName();
            Users users = userRepo.findByEmail(user);
            if (users == null) {
                throw new BadRequestException("User not found");
            }
            Message message = messageRepo.findById(messageId).orElseThrow(() -> new BadRequestException("Message not found"));
            if (message.getDeletedAt() != null) {
                throw new BadRequestException("Message already deleted");
            }
            if (message.getSenderId() != users.getId() || message.getReceiverId() != users.getId()) {
                throw new ForbiddenException("You are not authorized to delete this message");
            }
            message.setDeletedAt(LocalDateTime.now());
            messageRepo.save(message);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

}
