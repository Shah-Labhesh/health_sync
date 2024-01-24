package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSpecialityDto {

    @NotNull(message = "Speciality name is required")
    private String name;

    @NotNull(message = "Speciality image is required")
    private MultipartFile image;
}
