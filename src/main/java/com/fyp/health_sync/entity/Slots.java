package com.fyp.health_sync.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="slots")
@Data
public class Slots {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor", referencedColumnName = "id")
    private Users doctor;
    private LocalDateTime slotDateTime;
    private LocalDateTime endTime;
    private Boolean isExpired;
    private Boolean isBooked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
