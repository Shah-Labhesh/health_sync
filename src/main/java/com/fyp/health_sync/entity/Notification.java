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
    private UUID userId;
    private UUID doctorId;
    private UUID patientId;
    private UUID appointmentId;
    private UUID prescriptionId;
    private UUID medicalReportId;
    private UUID chatRoomId;
    private UUID paymentId;
    private UUID reviewId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime deletedAt;
}
