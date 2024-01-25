package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.AddSpecialityDto;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.AdminService;
import com.fyp.health_sync.service.SpecialityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final SpecialityService specialityService;

    @Operation(summary = "Update approved status of doctor")
    @PutMapping("/approve-status/{doctorId}/{status}")
    public ResponseEntity<?> updateApprovedStatus(@PathVariable UUID doctorId, @PathVariable Boolean status) throws BadRequestException, InternalServerErrorException {
        return adminService.updateApprovedStatus(doctorId, status);
    }

    @Operation(summary = "Update popular status of doctor")
    @PutMapping("/popular-status/{doctorId}/{status}")
    public ResponseEntity<?> updatePopularStatus(@PathVariable UUID doctorId, @PathVariable Boolean status) throws BadRequestException, InternalServerErrorException {
        return adminService.updatePopularStatus(doctorId, status);
    }

    @Operation(summary = "soft user delete")
    @PutMapping("/user-status/{userId}")
    public ResponseEntity<?> changeUserStatus(@PathVariable UUID userId, @RequestParam(required = true) UserStatus status) throws BadRequestException, InternalServerErrorException {
        return adminService.changeAccountStatus(userId, status);
    }



    @Operation(summary = "delete user permanently")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserPermanently(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException {
        return adminService.deleteUser(userId);
    }


    @Operation(summary = "restore user from soft delete")
    @PutMapping("/restore/{userId}")
    public ResponseEntity<?> restoreUser(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException {
        return adminService.restoreUser(userId);
    }



    @Operation(summary = "manage user")
    @GetMapping("/manage-user")
    public ResponseEntity<?> manageUser(@RequestParam(required = false, defaultValue = "ACTIVE") UserStatus status) throws InternalServerErrorException {
        return adminService.manageUser(status);
    }

    @Operation(summary = "manage Doctor")
    @GetMapping("/manage-doctor")
    public ResponseEntity<?> manageDoctor(@RequestParam(required = false, defaultValue = "ACTIVE") UserStatus status) throws InternalServerErrorException {
        return adminService.manageDoctor(status);
    }

    @Operation(summary = "get Dashboard data")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() throws InternalServerErrorException {
        return adminService.getDashboardData();
    }

    @Operation(summary = "get all specialities")
    @GetMapping("/specialities")
    public ResponseEntity<?> getAllSpecialities() throws InternalServerErrorException {
        return specialityService.getAllSpecialities();
    }

    @Operation(summary = "add speciality")
    @PostMapping("/speciality")
    public ResponseEntity<?> addSpeciality(@ModelAttribute @RequestBody @Valid AddSpecialityDto speciality) throws InternalServerErrorException {
        return specialityService.addSpeciality(speciality);
    }

    @Operation(summary = "update speciality")
    @PutMapping("/speciality/{specialityId}")
    public ResponseEntity<?> updateSpeciality(@ModelAttribute @RequestBody AddSpecialityDto speciality, @PathVariable UUID specialityId) throws InternalServerErrorException {
        return specialityService.updateSpeciality(speciality, specialityId);
    }

    @Operation(summary = "delete speciality")
    @DeleteMapping("/speciality/{specialityId}")
    public ResponseEntity<?> deleteSpeciality(@PathVariable UUID specialityId) throws InternalServerErrorException {
        return specialityService.deleteSpeciality(specialityId);
    }
}
