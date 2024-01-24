package com.fyp.health_sync.dtos;


import com.fyp.health_sync.enums.AppointmentType;
import com.fyp.health_sync.validation.EnumValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TakeAppointmentDto {
    @NotNull(message = "doctor id is cannot be empty")
    private UUID doctorId;
    @NotNull(message = "slot id is cannot be empty")
    private UUID slotId;
    @EnumValidator(enumClass = AppointmentType.class, message = "Appointment type must be CONSULTATION, FOLLOWUP,EMERGENCY,THERAPY,COUNSELING")
    private String appointmentType;
    @Size(min = 10, max = 250, message = "notes must be between 10 and 250 digits")
    private String notes;
}
