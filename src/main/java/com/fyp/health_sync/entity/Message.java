package com.fyp.health_sync.entity;

import com.fyp.health_sync.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="message")
public class Message {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String message;
    private byte[] file;
    private UUID senderId;
    private UUID receiverId;
    private MessageType messageType;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "chatRoom", referencedColumnName = "id")
    private ChatRoom chatRoom;
}
