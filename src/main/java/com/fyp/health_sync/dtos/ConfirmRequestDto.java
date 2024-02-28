package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmRequestDto {

    private String public_key;
    @NotBlank(message = "Token is required")
    private String token;
    private Integer amount;
    @NotBlank(message = "Confirmation code is required")
    private String confirmation_code;
    @NotBlank(message = "Transaction Pin is required")
    private String transaction_pin;
    @NotNull(message = "appointment Id is required")
    private UUID appointmentId;
    @NotBlank(message = "Mobile number is required")
    private String mobile;
}
