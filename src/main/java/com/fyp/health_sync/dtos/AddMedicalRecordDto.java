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
            message = "Record type should be one of the following: IMAGE,PDF"
    )
    private String recordType;
    private MultipartFile record;
    @NotBlank(message = "Record created date cannot be empty")
    private String recordCreatedDate;
}
