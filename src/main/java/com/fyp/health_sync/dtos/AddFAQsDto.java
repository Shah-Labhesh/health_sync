package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddFAQsDto {
    
    @NotBlank(message = "Question is required")
    private String question;
    @NotBlank(message = "Answer is required")
    private String answer;
}
