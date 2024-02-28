package com.fyp.health_sync.service;


import com.fyp.health_sync.dtos.RatingDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.RatingReviews;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.RatingType;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.RatingRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.RatingResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepo ratingRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> rate(UUID targetId, RatingDto rating) throws BadRequestException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users user = userRepo.findByEmail(authentication.getName());

            if (user == null) {
                throw new BadRequestException("User not found");
            }

            RatingType ratingType = switch (rating.getRatingType()) {
                case "DOCTOR" -> RatingType.DOCTOR;
                case "USER" -> RatingType.USER;
                default -> throw new BadRequestException("Invalid rating type");
            };

            Users targetEntity = userRepo.findById(targetId)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            ratingRepo.save(buildRatingReviews(user, targetEntity, ratingType, rating));
            return ResponseEntity.created(null).body(new SuccessResponse(getRatingSuccessMessage(ratingType)));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
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
        } else {
            ratings.setUser((Users) targetEntity);
        }

        return ratings;
    }

    private String getRatingSuccessMessage(RatingType ratingType) {
        return switch (ratingType) {
            case DOCTOR -> "Doctor rated successfully";
            case USER -> "User rated successfully";
            default -> "Rating successful";
        };
    }

    public ResponseEntity<?> getRatings(UUID targetId, String ratingType) throws BadRequestException, InternalServerErrorException {
        try {
            RatingType ratingType1 = switch (ratingType) {
                case "DOCTOR" -> RatingType.DOCTOR;
                case "USER" -> RatingType.USER;
                default -> throw new BadRequestException("Invalid rating type");
            };
            List<RatingResponse> response = new ArrayList<>();

            if (ratingType1 == RatingType.DOCTOR) {
                Users user = userRepo.findById(targetId)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                for (RatingReviews rating : ratingRepo.findAllByDoctorAndRatingType(user, ratingType1)) {
                    response.add(new RatingResponse().castToResponse(rating));
                }
                return ResponseEntity.ok(response);
            }else {
                Users user = userRepo.findById(targetId)
                        .orElseThrow(() -> new BadRequestException("User not found"));
                for (RatingReviews rating : ratingRepo.findAllByUserAndRatingType(user, ratingType1)) {
                    response.add(new RatingResponse().castToResponse(rating));
                }
                return ResponseEntity.ok(response);
            }


        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

}
