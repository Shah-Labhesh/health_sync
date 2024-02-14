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

    @Query("SELECT AVG(r.ratings) FROM RatingReviews r WHERE r.doctor = :user AND r.ratingType = 'DOCTOR' ")
    Double getAverageRatingForUser(@Param("user") Users user);

    Integer countAllByDoctorIdAndRatingType(UUID id, RatingType ratingType);

    RatingReviews findByUserAndDoctorAndRatingType(Users user, Users doctor, RatingType ratingType);

    RatingReviews findByUserAndAppointmentAndRatingType(Users user, Appointments appointment, RatingType ratingType);

    List<RatingReviews> findAllByDoctorAndRatingType(Users doctor, RatingType ratingType);

    List<RatingReviews> findAllByUserAndRatingType(Users user, RatingType ratingType1);

    List<RatingReviews> findAllByAppointmentAndRatingType(Appointments appointment, RatingType ratingType1);
}
