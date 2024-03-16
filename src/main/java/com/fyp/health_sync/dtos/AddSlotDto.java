package com.fyp.health_sync.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSlotDto {

    @NotNull(message = "Slot date is cannot be empty")
    
    private String slotDateTime;
}
