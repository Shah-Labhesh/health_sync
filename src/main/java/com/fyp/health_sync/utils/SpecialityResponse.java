package com.fyp.health_sync.utils;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialityResponse {
    private UUID id;
    private String name;
    private String image;

    public SpecialityResponse castToResponse(com.fyp.health_sync.entity.Speciality speciality) {
        return SpecialityResponse.builder()
                .id(speciality.getId())
                .name(speciality.getName())
                .image(speciality.getImage() != null ? "speciality/"+speciality.getId() : null)
                .build();
    }
}
