package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotsResponse {


    private UUID id;
    private DoctorResponse doctor;
    private LocalDateTime slotDateTime;
    private Boolean isBooked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SlotsResponse castToResponse(Slots slots){
        return SlotsResponse.builder()
                .id(slots.getId())
                .doctor(new DoctorResponse().castToResponse(slots.getDoctor()))
                .slotDateTime(slots.getSlotDateTime())
                .isBooked(slots.getIsBooked())
                .createdAt(slots.getCreatedAt())
                .updatedAt(slots.getUpdatedAt())
                .build();
    }
}
