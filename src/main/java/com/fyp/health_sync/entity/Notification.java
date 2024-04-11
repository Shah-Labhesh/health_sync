package com.fyp.health_sync.entity;


import com.fyp.health_sync.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "notifications")
public class Notification {


    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String title;
    private String body;
    private NotificationType type;
    private boolean isRead;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "receiver", referencedColumnName = "id")
    private Users receiver;
    private UUID targetId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime deletedAt;
}
