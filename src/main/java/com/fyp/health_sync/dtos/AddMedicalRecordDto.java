package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMedicalRecordDto {

    @NotBlank(message = "Record type cannot be empty")
    @EnumValidator(
            enumClass = RecordType.class,
            message = "Record type should be one of the following: IMAGE,DOCUMENT,TEXT"
    )
    private RecordType recordType;
    private MultipartFile record;
    @Size(min = 20, max = 1000, message = "Record text should be between 20 to 1000 characters")
    private String recordText;
}
