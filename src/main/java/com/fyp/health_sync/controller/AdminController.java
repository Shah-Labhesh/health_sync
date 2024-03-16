package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.AddSpecialityDto;
import com.fyp.health_sync.dtos.UpdateSpecialityDto;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.AdminService;
import com.fyp.health_sync.service.PaymentService;
import com.fyp.health_sync.service.RatingService;
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
    private final RatingService ratingService;
    private final PaymentService paymentService;

    @Operation(summary = "Update approved status of doctor", description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/approve-status/{doctorId}/{status}")
    public ResponseEntity<?> updateApprovedStatus(@PathVariable UUID doctorId, @PathVariable Boolean status, @RequestParam(required = false) String message) throws BadRequestException, InternalServerErrorException {
        return adminService.updateApprovedStatus(doctorId, status, message);
    }

    @Operation(summary = "Update popular status of doctor",description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/popular-status/{doctorId}/{status}")
    public ResponseEntity<?> updatePopularStatus(@PathVariable UUID doctorId, @PathVariable Boolean status) throws BadRequestException, InternalServerErrorException {
        return adminService.updatePopularStatus(doctorId, status);
    }

    @Operation(summary = "soft user delete",description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/user-status/{userId}")
    public ResponseEntity<?> changeUserStatus(@PathVariable UUID userId, @RequestParam(required = true) UserStatus status) throws BadRequestException, InternalServerErrorException {
        return adminService.changeAccountStatus(userId, status);
    }



    @Operation(summary = "delete user permanently",description = "UserRole.ADMIN", tags = {"Admin"})
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserPermanently(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException {
        return adminService.deleteUser(userId);
    }


    @Operation(summary = "restore user from soft delete",description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/restore/{userId}")
    public ResponseEntity<?> restoreUser(@PathVariable UUID userId) throws BadRequestException, InternalServerErrorException {
        return adminService.restoreUser(userId);
    }



    @Operation(summary = "manage user",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/manage-user")
    public ResponseEntity<?> manageUser(@RequestParam(required = false, defaultValue = "ACTIVE") UserStatus status) throws InternalServerErrorException {
        return adminService.manageUser(status);
    }

    @Operation(summary = "manage Doctor",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/manage-doctor")
    public ResponseEntity<?> manageDoctor(@RequestParam(required = false, defaultValue = "ACTIVE") UserStatus status) throws InternalServerErrorException {
        return adminService.manageDoctor(status);
    }

    @Operation(summary = "get Dashboard data",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() throws InternalServerErrorException {
        return adminService.getDashboardData();
    }

    @Operation(summary = "get all specialities",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/specialities")
    public ResponseEntity<?> getAllSpecialities() throws InternalServerErrorException {
        return specialityService.getAllSpecialities();
    }

    @Operation(summary = "add speciality",description = "UserRole.ADMIN", tags = {"Admin"})
    @PostMapping("/speciality")
    public ResponseEntity<?> addSpeciality(@ModelAttribute @RequestBody @Valid AddSpecialityDto speciality) throws InternalServerErrorException {
        return specialityService.addSpeciality(speciality);
    }

    @Operation(summary = "update speciality",description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/speciality/{specialityId}")
    public ResponseEntity<?> updateSpeciality(@ModelAttribute @RequestBody UpdateSpecialityDto speciality, @PathVariable UUID specialityId) throws InternalServerErrorException, BadRequestException {
        return specialityService.updateSpeciality(speciality, specialityId);
    }

    @Operation(summary = "delete speciality",description = "UserRole.ADMIN", tags = {"Admin"})
    @DeleteMapping("/speciality/{specialityId}")
    public ResponseEntity<?> deleteSpeciality(@PathVariable UUID specialityId) throws InternalServerErrorException, BadRequestException {
        return specialityService.deleteSpeciality(specialityId);
    }

    @Operation(summary = "rating of user or doctor",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/ratings/{userId}/{ratingType}")
    public ResponseEntity<?> getRatings(@PathVariable UUID userId, @PathVariable String ratingType) throws BadRequestException, InternalServerErrorException {
        return ratingService.getRatings(userId, ratingType);
    }

    @Operation(summary = "get all data removal requests",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/data-removal-requests")
    public ResponseEntity<?> getAllDataRemovalRequests() throws InternalServerErrorException {
        return adminService.getAllDataRemovalRequests();
    }

    @Operation(summary = "accept data removal request",description = "UserRole.ADMIN", tags = {"Admin"})
    @DeleteMapping("/remove-data/{requestId}")
    public ResponseEntity<?> acceptDataRemovalRequest(@PathVariable UUID requestId) throws BadRequestException, InternalServerErrorException {
        return adminService.acceptDataRemovalRequest(requestId);
    }

    @Operation(summary = "reject data removal request",description = "UserRole.ADMIN", tags = {"Admin"})
    @PutMapping("/reject-data-removal/{requestId}")
    public ResponseEntity<?> rejectDataRemovalRequest(@PathVariable UUID requestId) throws BadRequestException, InternalServerErrorException {
        return adminService.rejectDataRemovalRequest(requestId);
    }

    @Operation(summary = "get all payments",description = "UserRole.ADMIN", tags = {"Admin"})
    @GetMapping("/payments")
    public ResponseEntity<?> getAllPayments() throws InternalServerErrorException, ForbiddenException, BadRequestException {
        return paymentService.getAllPayments();
    }
}
