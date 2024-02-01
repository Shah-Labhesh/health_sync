package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.*;
import com.fyp.health_sync.enums.RatingType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.repository.*;
import com.fyp.health_sync.utils.*;
import jakarta.transaction.Transactional;
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
public class DoctorService {

    private final UserRepo userRepo;
    private final FavoritesRepo favoritesRepo;
    private final QualificationRepo qualificationRepo;
    private final AppointmentRepo appointmentRepo;
    private final RatingRepo ratingRepo;

    public ResponseEntity<?> getNearbyDoctors(double latitude, double longitude) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepo.findByEmail(authentication.getName());
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        List<Users> nearByDoctors = userRepo.findNearbyDoctors(latitude, longitude, 30);
        List<DoctorResponse> responses = new ArrayList<>();
        for (Users nearByDoctor : nearByDoctors
        ) {

            DoctorResponse doctorResponse = new DoctorResponse().castToResponse(nearByDoctor);
            if (favoritesRepo.findByDoctorAndUser(nearByDoctor, user) != null) {
                doctorResponse.setFavorite(true);
            } else {
                doctorResponse.setFavorite(false);
            }
            doctorResponse.setAvgRatings(ratingRepo.getAverageRatingForUser(nearByDoctor) != null ? ratingRepo.getAverageRatingForUser(nearByDoctor) : 0.0);
            doctorResponse.setRatingCount(ratingRepo.countAllByDoctorId(nearByDoctor.getId()));
            responses.add(doctorResponse);

        }
        return ResponseEntity.ok(responses);
    }

    @Transactional
    public ResponseEntity<?> toggleFavorite(UUID doctorId) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepo.findByEmail(authentication.getName());
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("You can only add doctors to favorites");
        }
        if (favoritesRepo.findByDoctorAndUser(doctor, user) != null) {
            favoritesRepo.delete(favoritesRepo.findByDoctorAndUser(doctor, user));
            return ResponseEntity.ok(new SuccessResponse("Removed from favorites"));
        } else {
            favoritesRepo.save(Favorites.builder()
                    .doctor(doctor)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build());
            return ResponseEntity.ok(new SuccessResponse("Added to favorites"));
        }
    }

    public ResponseEntity<?> getMyFavorites() throws BadRequestException, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userRepo.findByEmail(authentication.getName());
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        if (user.getRole() != UserRole.USER) {
            throw new ForbiddenException("You are not authorized to get favorites");
        }
        List<Favorites> favorites = favoritesRepo.findAllByUser(user);
        List<DoctorResponse> responses = new ArrayList<>();
        for (Favorites favorite : favorites
        ) {
            Users doctor = favorite.getDoctor();
            DoctorResponse doctorResponse = new DoctorResponse().castToResponse(doctor);
            doctorResponse.setFavorite(true);
            doctorResponse.setAvgRatings(ratingRepo.getAverageRatingForUser(doctor) != null ? ratingRepo.getAverageRatingForUser(doctor) : 0.0);
            doctorResponse.setRatingCount(ratingRepo.countAllByDoctorId(doctor.getId()));
            responses.add(doctorResponse);
        }
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<?> getDoctorById(UUID id) throws BadRequestException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("This user is not a doctor");
        }

        DoctorResponse response = new DoctorResponse().castToResponse(doctor);

        if (favoritesRepo.findByDoctorAndUser(doctor, user) != null) {
            response.setFavorite(true);
        } else {
            response.setFavorite(false);
        }
        response.setAvgRatings(ratingRepo.getAverageRatingForUser(doctor) != null ? ratingRepo.getAverageRatingForUser(doctor) : 0.0);
        response.setRatingCount(ratingRepo.countAllByDoctorId(doctor.getId()));

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getDoctorQualification(UUID id) throws BadRequestException {
        Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("This user is not a doctor");
        }
        List<Qualifications> list = qualificationRepo.findAllByDoctor(doctor);
        List<QualificationResponse> response = new ArrayList<>();
        for (Qualifications qualification : list
        ) {
            response.add(new QualificationResponse().castToResponse(qualification));
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getRatingsOfDoctor(UUID id) throws BadRequestException {
        Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new BadRequestException("This user is not a doctor");
        }
        List<RatingReviews> list = ratingRepo.findAllByDoctorAndRatingType(doctor, RatingType.DOCTOR);
        List<RatingResponse> response = new ArrayList<>();

        for (RatingReviews rating : list
        ) {
            response.add(new RatingResponse().castToResponse(rating));
        }

        return ResponseEntity.ok(response);
    }

    // get my patient list from appointment
    public ResponseEntity<?> getMyPatientList() throws BadRequestException, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users doctor = userRepo.findByEmail(authentication.getName());
        if (doctor == null) {
            throw new BadRequestException("User not found");
        }
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ForbiddenException("You are not authorized to get patient list");
        }
        List<Appointments> appointments = appointmentRepo.findAllByDoctor(doctor);
        List<UserResponse> responses = new ArrayList<>();
        for (Appointments appointment : appointments
        ) {
            if (appointment.getUser().getStatus() != UserStatus.ACTIVE){
                continue;
            }

            responses.add(new UserResponse().castToResponse(appointment.getUser()));
        }
        return ResponseEntity.ok(responses);
    }

    // get my appointment list from appointments order by slotDateTime
    public ResponseEntity<?> getMyAppointments() throws BadRequestException, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users doctor = userRepo.findByEmail(authentication.getName());
        if (doctor == null) {
            throw new BadRequestException("User not found");
        }
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new ForbiddenException("You are not authorized to get patient list");
        }
        List<Appointments> appointments = appointmentRepo.findAllByDoctor(doctor);
        List<AppointmentResponse> responses = new ArrayList<>();
        for (Appointments appointment : appointments
        ) {
            responses.add(new AppointmentResponse().castToResponse(appointment));
        }

        responses.sort((o1, o2) -> o1.getSlot().getSlotDateTime().compareTo(o2.getSlot().getSlotDateTime()));
        return ResponseEntity.ok(responses);
    }
}
