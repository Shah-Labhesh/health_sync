package com.fyp.health_sync.entity;

import com.fyp.health_sync.enums.RatingType;
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
@Table(name="rating_reviews")
@Builder
public class RatingReviews {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    private double ratings;
    private String comment;
    @Enumerated (EnumType.STRING)
    private RatingType ratingType;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private Doctors doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private Users userId;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "appointmentId", referencedColumnName = "appointmentId")
    private Appointments appointmentId;

}


