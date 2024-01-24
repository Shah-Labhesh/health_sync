package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import org.springframework.web.multipart.MultipartFile;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualificationDto {


    @NotBlank(message = "Title is cannot be empty")
    private String title;
    @NotBlank(message = "Institute is cannot be empty")
    private String institute;
    @NotBlank(message = "Pass out year is cannot be empty")
    private String passOutYear;
    private MultipartFile certificate;

}
