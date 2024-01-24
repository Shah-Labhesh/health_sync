package com.fyp.health_sync.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMoreDetailsDto {

    @NotBlank(message = "Khalti Id cannot be empty")
    @Pattern(regexp = "^(\\+)?(977)?([0-9]{10})$", message = "Invalid Khalti Id")
    private String khaltiId;
}
