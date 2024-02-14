package com.fyp.health_sync.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFAQsDto {

    private String question;
    private String answer;
    
}
