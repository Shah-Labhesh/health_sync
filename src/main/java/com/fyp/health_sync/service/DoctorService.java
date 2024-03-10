package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.*;
import com.fyp.health_sync.enums.RatingType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
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

    public ResponseEntity<?> getNearbyDoctors(double latitude, double longitude) throws BadRequestException, InternalServerErrorException {
        try {
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
                doctorResponse.setRatingCount(ratingRepo.countAllByDoctorIdAndRatingType(nearByDoctor.getId(), RatingType.DOCTOR));
                responses.add(doctorResponse);

            }
            return ResponseEntity.ok(responses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> toggleFavorite(UUID doctorId) throws BadRequestException, InternalServerErrorException {
        try {
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
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getMyFavorites() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
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
                doctorResponse.setRatingCount(ratingRepo.countAllByDoctorIdAndRatingType(doctor.getId(), RatingType.DOCTOR));
                responses.add(doctorResponse);
            }
            return ResponseEntity.ok(responses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getDoctorById(UUID id) throws BadRequestException, InternalServerErrorException {
        try {
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
            response.setRatingCount(ratingRepo.countAllByDoctorIdAndRatingType(doctor.getId(), RatingType.DOCTOR));

            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getDoctorQualification(UUID id) throws BadRequestException, InternalServerErrorException {
        try {
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
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getRatingsOfDoctor(UUID id) throws BadRequestException, InternalServerErrorException {
        try {
            Users doctor = userRepo.findById(id).orElseThrow(() -> new BadRequestException("Doctor not found"));
            if (doctor.getRole() != UserRole.DOCTOR) {
                throw new BadRequestException("This user is not a doctor");
            }
            List<RatingReviews> list = new ArrayList<>();
            List<RatingResponse> response = new ArrayList<>();

            for (RatingReviews rating : ratingRepo.findAllByDoctorAndRatingType(doctor, RatingType.DOCTOR)
            ) {
                list.add(rating);
            }

            // at first group by user and then add latest rating to list
            for (RatingReviews rating : list
            ) {
                boolean isFound = false;
                for (RatingResponse ratingResponse : response
                ) {
                    if (ratingResponse.getUser().getId().equals(rating.getUser().getId())) {
                        isFound = true;
                        if (ratingResponse.getCreatedAt().isBefore(rating.getCreatedAt())) {
                            ratingResponse.setCreatedAt(rating.getCreatedAt());
                            ratingResponse.setRatings(rating.getRatings());
                            ratingResponse.setComment(rating.getComment());
                        }
                        break;
                    }
                }
                if (!isFound) {
                    response.add(new RatingResponse().castToResponse(rating));
                }
            }


            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // get my patient list from appointment
    public ResponseEntity<?> getMyPatientList() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
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
                if (appointment.getUser().getStatus() != UserStatus.ACTIVE) {
                    continue;
                }

                responses.add(new UserResponse().castToResponse(appointment.getUser()));
            }
            // remove duplicates
            List<UserResponse> uniqueList = new ArrayList<>();
            for (UserResponse userResponse : responses) {
                boolean isFound = false;
                for (UserResponse userResponse1 : uniqueList) {
                    if (userResponse.getId().equals(userResponse1.getId())) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) uniqueList.add(userResponse);
            }
            return ResponseEntity.ok(uniqueList);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }


    // get talked doctor list from appointment
    public ResponseEntity<?> getTalkedDoctorList() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Users user = userRepo.findByEmail(authentication.getName());
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("You are not authorized to get talked doctor list");
            }
            List<Appointments> appointments = appointmentRepo.findAllByUser(user);
            List<DoctorResponse> responses = new ArrayList<>();
            for (Appointments appointment : appointments
            ) {
                if (appointment.getDoctor().getStatus() != UserStatus.ACTIVE) {
                    continue;
                }
                DoctorResponse doctorResponse = new DoctorResponse().castToResponse(appointment.getDoctor());
                doctorResponse.setAvgRatings(ratingRepo.getAverageRatingForUser(appointment.getDoctor()) != null ? ratingRepo.getAverageRatingForUser(appointment.getDoctor()) : 0.0);
                doctorResponse.setRatingCount(ratingRepo.countAllByDoctorIdAndRatingType(appointment.getDoctor().getId(), RatingType.DOCTOR));
                responses.add(doctorResponse);
            }
            // remove duplicates
            List<DoctorResponse> uniqueList = new ArrayList<>();
            for (DoctorResponse doctorResponse : responses) {
                boolean isFound = false;
                for (DoctorResponse doctorResponse1 : uniqueList) {
                    if (doctorResponse.getId().equals(doctorResponse1.getId())) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) uniqueList.add(doctorResponse);
            }
            return ResponseEntity.ok(uniqueList);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> filterDoctors(double latitude, double longitude, String text, UUID speciality, String feeType, Integer feeFrom, Integer feeTo, Double ratings, Boolean popular) throws BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            List<Users> doctors = userRepo.findNearbyDoctors(latitude, longitude, 30);
            if (text != null) {
                doctors.removeIf(doctor -> !doctor.getName().toLowerCase().contains(text.toLowerCase()));
            }
            if (speciality != null) {
                doctors.removeIf(doctor -> doctor.getSpeciality() == null);
                doctors.removeIf(doctor -> !doctor.getSpeciality().getId().equals(speciality));
            }
            if (feeType != null) {
                doctors.removeIf(doctor -> doctor.getFee() == null);
                switch (feeType) {
                    case "LOW_TO_HIGH":
                        doctors.sort((o1, o2) -> o1.getFee().compareTo(o2.getFee()));
                        break;
                    case "HIGH_TO_LOW":
                        doctors.sort((o1, o2) -> o2.getFee().compareTo(o1.getFee()));
                        break;
                    case "RANGE":
                        if (feeFrom == null || feeTo == null) {
                            throw new BadRequestException("Fee range is required");
                        }
                        if (feeFrom >= feeTo) {
                            throw new BadRequestException("Invalid fee range");
                        }
                        doctors.removeIf(doctor -> doctor.getFee() < feeFrom || doctor.getFee() > feeTo);
                        break;
                }
            }
            if (ratings != null || ratings != 0.0) {
                doctors.removeIf(doctor -> ratingRepo.getAverageRatingForUser(doctor) == null || ratingRepo.getAverageRatingForUser(doctor) < ratings);
            }
            if (popular != null) {
                doctors.removeIf(doctor -> doctor.isPopular() != popular);
            }
            List<DoctorResponse> responses = new ArrayList<>();

            for (Users doctor : doctors
            ) {
                DoctorResponse doctorResponse = new DoctorResponse().castToResponse(doctor);
                doctorResponse.setAvgRatings(ratingRepo.getAverageRatingForUser(doctor) != null ? ratingRepo.getAverageRatingForUser(doctor) : 0.0);
                doctorResponse.setRatingCount(ratingRepo.countAllByDoctorIdAndRatingType(doctor.getId(), RatingType.DOCTOR));
                doctorResponse.setFavorite(favoritesRepo.findByDoctorAndUser(doctor, user) != null);
                responses.add(doctorResponse);
            }
            return ResponseEntity.ok(responses);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
