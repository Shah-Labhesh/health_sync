package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.MultipartFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import java.util.zip.DataFormatException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class MultipartFileController {

    private final MultipartFileService multipartFileService;

    @GetMapping("/get-avatar/{userId}")
    public ResponseEntity<?> getAvatar(@PathVariable UUID userId) throws BadRequestException, DataFormatException, IOException, InternalServerErrorException {
        return multipartFileService.getAvatarById(userId);
    }
    @GetMapping("/certificate/{qualificationId}")
    public ResponseEntity<?> getCertificate(@PathVariable UUID qualificationId) throws BadRequestException, DataFormatException, InternalServerErrorException {
        return multipartFileService.getCertificate(qualificationId);
    }

    @GetMapping("/image-record/{recordId}")
    public ResponseEntity<?> getImageRecord(@PathVariable UUID recordId) throws BadRequestException, DataFormatException, InternalServerErrorException {
        return multipartFileService.getImageRecord(recordId);
    }

    @GetMapping("/pdf-record/{recordId}")
    public ResponseEntity<?> getPdfRecord(@PathVariable UUID recordId) throws BadRequestException, DataFormatException, InternalServerErrorException {
        return multipartFileService.getPdfRecord(recordId);
    }

    @GetMapping("/speciality/{specialityId}")
    public ResponseEntity<?> getSpecialityImage(@PathVariable UUID specialityId) throws BadRequestException, DataFormatException, IOException, InternalServerErrorException {
        return multipartFileService.getSpecialityImage(specialityId);
    }

    @GetMapping("/prescription/{prescriptionId}")
    public ResponseEntity<?> getPrescription(@PathVariable UUID prescriptionId) throws BadRequestException, DataFormatException, IOException, InternalServerErrorException {
        return multipartFileService.getPrescription(prescriptionId);
    }
}
