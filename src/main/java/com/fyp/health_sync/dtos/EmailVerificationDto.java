package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationDto {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "OTP cannot be empty")
    @Size(min = 4, max = 4, message = "OTP should be of 4 digits")
    @Pattern(regexp = "[0-9]+", message = "OTP should be numeric")
    private String otp;
}
