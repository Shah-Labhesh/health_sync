package com.fyp.health_sync.service;


import com.fyp.health_sync.dtos.RatingDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.RatingReviews;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.RatingType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.AppointmentRepo;
import com.fyp.health_sync.repository.RatingRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepo ratingRepo;
    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;

    public ResponseEntity<?> rate(UUID targetId, RatingDto rating) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepo.findByEmail(authentication.getName());

        if (user == null) {
            throw new BadRequestException("User not found");
        }

        RatingType ratingType = switch (rating.getRatingType()) {
            case "DOCTOR" -> RatingType.DOCTOR;
            case "USER" -> RatingType.USER;
            case "APPOINTMENT" -> RatingType.APPOINTMENT;
            default -> throw new BadRequestException("Invalid rating type");
        };

        Object targetEntity;
        if (ratingType == RatingType.DOCTOR || ratingType == RatingType.USER) {
            targetEntity = userRepo.findById(targetId)
                    .orElseThrow(() -> new BadRequestException("User not found"));
        } else {
            targetEntity = appointmentRepo.findById(targetId)
                    .orElseThrow(() -> new BadRequestException("Appointment not found"));
        }

        RatingReviews ratingReviews = getRatingReviews(user, targetEntity, ratingType);
        if (ratingReviews != null) {
            ratingReviews.setRatings(rating.getRatings());
            ratingReviews.setComment(rating.getComment());
            ratingReviews.setUpdatedAt(LocalDateTime.now());
            ratingRepo.save(ratingReviews);
            return ResponseEntity.ok(new SuccessResponse(getRatingSuccessMessage(ratingType)));
        }

        ratingRepo.save(buildRatingReviews(user, targetEntity, ratingType, rating));
        return ResponseEntity.ok(new SuccessResponse(getRatingSuccessMessage(ratingType)));
    }

    private RatingReviews getRatingReviews(Users user, Object targetEntity, RatingType ratingType) {
        if (ratingType == RatingType.DOCTOR || ratingType == RatingType.USER) {
            return ratingRepo.findByUserAndDoctorAndRatingType(user, (Users) targetEntity, ratingType);
        } else if (ratingType == RatingType.APPOINTMENT) {
            return ratingRepo.findByUserAndAppointmentAndRatingType(user, (Appointments) targetEntity, ratingType);
        }
        return null;
    }

    private RatingReviews buildRatingReviews(Users user, Object targetEntity, RatingType ratingType, RatingDto rating) {
        RatingReviews ratings = RatingReviews.builder()
                .ratings(rating.getRatings())
                .comment(rating.getComment())
                .ratingType(ratingType)
                .createdAt(LocalDateTime.now())
                .user(user).build();

        if (ratingType == RatingType.DOCTOR) {
            ratings.setDoctor((Users) targetEntity);
        } else if (ratingType == RatingType.APPOINTMENT) {
            ratings.setAppointment((Appointments) targetEntity);
        } else {
            ratings.setUser((Users) targetEntity);
        }

        return ratings;
    }

    private String getRatingSuccessMessage(RatingType ratingType) {
        return switch (ratingType) {
            case DOCTOR -> "Doctor rated successfully";
            case USER -> "User rated successfully";
            case APPOINTMENT -> "Appointment rated successfully";
            default -> "Rating successful";
        };
    }

}
