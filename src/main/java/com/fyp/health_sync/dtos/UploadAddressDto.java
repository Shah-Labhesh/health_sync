package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadAddressDto {

    @NotNull(message = "latitude is cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "latitude must be greater than 0.0")
    private double latitude;
    @NotNull(message = "longitude is cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "longitude must be greater than 0.0")
    private double longitude;



   }
