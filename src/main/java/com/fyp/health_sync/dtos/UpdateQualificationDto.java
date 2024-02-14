package com.fyp.health_sync.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQualificationDto {

    private String qualification;
    private String institute;
    private String passOutYear;
    private MultipartFile certificate;
}
