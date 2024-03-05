package com.fyp.health_sync.dtos;

import org.springframework.web.multipart.MultipartFile;

import com.fyp.health_sync.enums.MedicalRecordType;
import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UpdateMedicalRecordDto {

    @EnumValidator(enumClass = RecordType.class, message = "Record type should be one of the following: IMAGE,DOCUMENT,TEXT")
    private String recordType;

    private MultipartFile record;
    @EnumValidator(enumClass = MedicalRecordType.class, message = "Medical Record type should be one of the following: VITAL_SIGNS_RECORD,LAB_TEST_RESULTS,RADIOLOGY_REPORTS")
    private String medicalRecordType;

}
