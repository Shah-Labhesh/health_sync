package com.fyp.health_sync.utils;


import com.fyp.health_sync.entity.RatingReviews;
import com.fyp.health_sync.enums.RatingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingResponse {

    private UUID id;

    private double ratings;
    private String comment;
    @Enumerated(EnumType.STRING)
    private RatingType ratingType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private DoctorResponse doctor;

    private UserResponse user;


    public RatingResponse castToResponse(RatingReviews ratingReviews){
        return RatingResponse.builder()
                .id(ratingReviews.getId())
                .ratings(ratingReviews.getRatings())
                .comment(ratingReviews.getComment() != null ? ratingReviews.getComment() : "")
                .ratingType(ratingReviews.getRatingType())
                .createdAt(ratingReviews.getCreatedAt())
                .updatedAt(ratingReviews.getUpdatedAt())
                .doctor(ratingReviews.getDoctor() != null ? new DoctorResponse().castToResponse( ratingReviews.getDoctor() ) : null)
                .user(ratingReviews.getUser() != null ? new UserResponse().castToResponse(ratingReviews.getUser() ): null)
                .build();
    }
}
