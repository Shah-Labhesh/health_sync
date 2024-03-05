package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.MedicalRecordType;
import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Medical Record type cannot be empty")
    @EnumValidator(
            enumClass = MedicalRecordType.class,
            message = "Medical Record type should be one of the following: VITAL_SIGNS_RECORD,LAB_TEST_RESULTS,RADIOLOGY_REPORTS"
    )
    private String medicalRecordType;
}
