package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.Favorites;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.FavoritesRepo;
import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.DoctorResponse;
import com.fyp.health_sync.utils.ImageUtils;
import com.fyp.health_sync.utils.SuccessResponse;
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

   public ResponseEntity<?> getNearbyDoctors(double latitude, double longitude) throws BadRequestException {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         Users user = userRepo.findByEmail(authentication.getName());
         if (user == null){
             throw new BadRequestException("User not found");
         }
      List<Users> nearByDoctors = userRepo.findNearbyUsers(latitude, longitude, 30);
      List< DoctorResponse> responses = new ArrayList<>();
       for (Users nearByDoctor : nearByDoctors
            ) {
           if (nearByDoctor.getRole() == UserRole.DOCTOR){
               if (favoritesRepo.findByDoctorAndUser(nearByDoctor,user) != null)
               {
                   DoctorResponse doctorResponse = new DoctorResponse().castToResponse(nearByDoctor);
                     doctorResponse.setFavorite(true);
                        responses.add(doctorResponse);
               }else{
                     DoctorResponse doctorResponse = new DoctorResponse().castToResponse(nearByDoctor);
                     doctorResponse.setFavorite(false);
                     responses.add(doctorResponse);
               }
           }

       }
        return ResponseEntity.ok(responses);
   }

   @Transactional
   public ResponseEntity<?> toggleFavorite(UUID doctorId) throws BadRequestException {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       Users user = userRepo.findByEmail(authentication.getName());
       if (user == null){
           throw new BadRequestException("User not found");
       }
       Users doctor = userRepo.findById(doctorId).orElseThrow(() -> new BadRequestException("Doctor not found"));
       if (doctor.getRole() != UserRole.DOCTOR){
           throw new BadRequestException("You can only add doctors to favorites");
       }
       if (favoritesRepo.findByDoctorAndUser(doctor,user) != null){
           favoritesRepo.delete(favoritesRepo.findByDoctorAndUser(doctor,user));
           return ResponseEntity.ok(new SuccessResponse("Removed from favorites"));
       }else{
           favoritesRepo.save(Favorites.builder()
                           .doctor(doctor)
                           .user(user)
                           .createdAt(LocalDateTime.now())
                   .build());
           return ResponseEntity.ok(new SuccessResponse("Added to favorites"));
       }
   }
}
