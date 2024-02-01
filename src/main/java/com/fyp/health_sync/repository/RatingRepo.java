package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.RatingReviews;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepo extends JpaRepository<RatingReviews, UUID> {

    List<RatingReviews> findAllByDoctor(Users doctor) ;

    @Query("SELECT AVG(r.ratings) FROM RatingReviews r WHERE r.user = :user")
    Double getAverageRatingForUser(@Param("user") Users user);


    Integer countAllByDoctorId(UUID id);

    RatingReviews findByUserAndDoctorAndRatingType(Users user, Users doctor, RatingType ratingType);

    RatingReviews findByUserAndAppointmentAndRatingType(Users user, Appointments appointment, RatingType ratingType);

    List<RatingReviews> findAllByDoctorAndRatingType(Users doctor, RatingType ratingType);
}
