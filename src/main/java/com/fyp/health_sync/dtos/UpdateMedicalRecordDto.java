package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UpdateMedicalRecordDto {

    @EnumValidator(
            enumClass = RecordType.class,
            message = "Record type should be one of the following: IMAGE,DOCUMENT,TEXT"
    )
    private String recordType;

    private String record;

    private String recordCreatedDate;


}
