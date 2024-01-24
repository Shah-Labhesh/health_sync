package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.MedicalRecordTypeValidator;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMedicalRecordDto {

    @NotBlank(message = "Record type is cannot be empty")
    @MedicalRecordTypeValidator
    private RecordType recordType;
    private MultipartFile record;
    @Size(min = 20, max = 1000, message = "Record text should be between 20 to 1000 characters")
    private String recordText;
}
