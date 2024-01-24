package com.fyp.health_sync.service;

import com.fyp.health_sync.dtos.AddSpecialityDto;
import com.fyp.health_sync.entity.Speciality;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.utils.SpecialityResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecialityService {

    private final SpecialityRepo specialityRepo;

    public ResponseEntity<?> getAllSpecialities() throws InternalServerErrorException {
        try{
            List<Speciality> specialities = specialityRepo.findAllByDeletedAtIsNull();
            List<SpecialityResponse> specialityResponses = new ArrayList<>();
            for (Speciality speciality : specialities) {
                specialityResponses.add(new SpecialityResponse().castToResponse(speciality));
            }
            return ResponseEntity.ok(specialityResponses);
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> addSpeciality(AddSpecialityDto speciality) throws InternalServerErrorException {
        try{
            Speciality newSpeciality = Speciality.builder()
                    .name(speciality.getName())
                    .image(speciality.getImage().getBytes())
                    .build();
            specialityRepo.save(newSpeciality);
            return ResponseEntity.ok(new SuccessResponse("Speciality added successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateSpeciality(AddSpecialityDto speciality, UUID specialityId) throws InternalServerErrorException {
        try{
            Speciality speciality1 = specialityRepo.findById(specialityId).orElseThrow(() -> new BadRequestException("Speciality not found"));
            if (speciality.getName() != null) {
                speciality1.setName(speciality.getName());
            }
            if (speciality.getImage() != null) {
                speciality1.setImage(speciality.getImage().getBytes());
            }
            speciality1.setUpdatedAt(LocalDateTime.now());
            specialityRepo.save(speciality1);
            return ResponseEntity.ok(new SuccessResponse("Speciality updated successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> deleteSpeciality(UUID specialityId) throws InternalServerErrorException {
        try{
            Speciality speciality1 = specialityRepo.findById(specialityId).orElseThrow(() -> new BadRequestException("Speciality not found"));
            speciality1.setDeletedAt(LocalDateTime.now());
            return ResponseEntity.ok(new SuccessResponse("Speciality deleted successfully"));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
