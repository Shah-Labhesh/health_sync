package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
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
    private RecordType recordType;

    private String record;

    @Size(min = 20, max = 1000, message = "Record text should be between 20 to 1000 characters")
    private String recordText;


}
