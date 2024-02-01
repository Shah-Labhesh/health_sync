package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.service.MultipartFileService;
import com.fyp.health_sync.service.QualificationService;
import com.fyp.health_sync.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.DataFormatException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class MultipartFileController {

    private final MultipartFileService multipartFileService;

    @GetMapping("/get-avatar/{userId}")
    public ResponseEntity<?> getAvatar(@PathVariable UUID userId) throws BadRequestException, DataFormatException, IOException {
        return multipartFileService.getAvatarById(userId);
    }
    @GetMapping("/certificate/{qualificationId}")
    public ResponseEntity<?> getCertificate(@PathVariable UUID qualificationId) throws BadRequestException {
        return multipartFileService.getCertificate(qualificationId);
    }

    @GetMapping("/image-record/{recordId}")
    public ResponseEntity<?> getImageRecord(@PathVariable UUID recordId) throws BadRequestException {
        return multipartFileService.getImageRecord(recordId);
    }

    @GetMapping("/pdf-record/{recordId}")
    public ResponseEntity<?> getPdfRecord(@PathVariable UUID recordId) throws BadRequestException {
        return multipartFileService.getPdfRecord(recordId);
    }

    @GetMapping("/speciality/{specialityId}")
    public ResponseEntity<?> getSpecialityImage(@PathVariable UUID specialityId) throws BadRequestException, DataFormatException, IOException {
        return multipartFileService.getSpecialityImage(specialityId);
    }
}
