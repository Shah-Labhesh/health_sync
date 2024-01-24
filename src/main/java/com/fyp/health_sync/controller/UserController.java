package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.UpdateUserDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.Multipart;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.DataFormatException;

@RestController
@SecurityRequirement(name = "BearerAuth")

@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/current-user")
    public ResponseEntity<?> getAllUser() throws BadRequestException, DataFormatException {
        return userService.currentUser();
    }

    @PutMapping("/current-user")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UpdateUserDto user) throws BadRequestException {
        return userService.updateUser(user);
    }

    @PostMapping("upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestBody MultipartFile file) throws BadRequestException, IOException {
        return userService.uploadProfilePicture(file);
    }

    @GetMapping("profile-picture")
    public ResponseEntity<?> getProfilePicture() throws BadRequestException, InternalServerErrorException {
        return userService.getProfileImage();
    }


}
