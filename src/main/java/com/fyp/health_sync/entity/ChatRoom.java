package com.fyp.health_sync.entity;


import com.fyp.health_sync.enums.MessageType;
import com.fyp.health_sync.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String lastMessage;
    private UUID senderId;
    @Enumerated(EnumType.STRING)
    private UserRole deletedBy;
    private LocalDateTime lastMessageAt;
    private MessageType messageType;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor", referencedColumnName = "id")
    private Users doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", referencedColumnName = "id")
    private Users user;

}
