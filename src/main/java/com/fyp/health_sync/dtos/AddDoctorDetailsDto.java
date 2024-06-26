package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddDoctorDetailsDto {

    @NotBlank(message = "speciality cannot be empty")
    private UUID speciality;
    @NotBlank(message = "Experience cannot be empty")
    @Size(min = 10, max = 250, message = "Experience must be between 10 and 250 digits")
    private String experience;
    @NotNull(message = "Fee cannot be empty")
    @Min(value = 100, message = "Fee must be greater than 0")
    private Integer fee;
    @NotNull(message = "Image cannot be empty")
    private MultipartFile image;
}
