package com.fyp.health_sync.entity;

import com.fyp.health_sync.enums.AppointmentType;
import com.fyp.health_sync.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="appointments")
@Builder
public class Appointments {


    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    private String appointmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor", referencedColumnName = "id")
    private Users doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"user\"", referencedColumnName = "id")
    private Users user;
    private String appointmentType;
    @OneToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "slot", referencedColumnName = "id")
    private Slots slot;
    private String notes;
    private Boolean isExpired;

    @Enumerated (EnumType.STRING)
    private PaymentStatus paymentStatus;
    private Integer appointmentFee;
    private Integer platformCost;
    private Integer totalFee;
    private LocalDateTime reminderTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
