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
    private UUID appointmentId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private Doctors doctorId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private Users UserId;
    private String appointmentType;
    @OneToOne (fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "slotId", referencedColumnName = "slotId")
    private Slots slotId;
    private String notes;

    @OneToOne (fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "paymentId", referencedColumnName = "id")
    private Payment paymentId;
    @Enumerated (EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
