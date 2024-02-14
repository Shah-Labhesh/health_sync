package com.fyp.health_sync.dtos;


import com.fyp.health_sync.enums.RecordType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadPrescriptionDto {

    private MultipartFile prescription;
    @NotNull(message = "User id is required")
    private UUID userId;
    @NotBlank(message = "Record type is required")
    @EnumValidator(
            enumClass = RecordType.class,
            message = "Record type must be IMAGE, PDF or TEXT"
    )
    private String recordType;
    @Size(min = 10, max = 1000, message = "Record text must be less than 1000 characters")
    private String prescriptionText;

}
