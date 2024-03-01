package com.fyp.health_sync.dtos;

import com.fyp.health_sync.enums.RemovalType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRemovalRequestDto {

    @NotBlank(message = "Type is required")
    @EnumValidator(
            enumClass = RemovalType.class,
            message = "Invalid removal type, must be one of {ACCOUNT_DELETION, MEDICAL_RECORDS_DELETION, PRESCRIPTION_DELETION}"
    )
    private String type;

    @NotBlank(message = "Reason is required")
    private String reason;
}
