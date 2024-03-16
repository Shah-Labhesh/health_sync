package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.ViewPrescriptionPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionPermissionResponse {
    private UUID id;

    private DoctorResponse doctor;

    private UserResponse user;

    private boolean isAccepted;
    private boolean isRejected;

    public PrescriptionPermissionResponse castToResponse(ViewPrescriptionPermission permission){
        return PrescriptionPermissionResponse.builder()
                .id(permission.getId())
                .doctor(permission.getDoctor() != null ? new DoctorResponse().castToResponse(permission.getDoctor()) : null)
                .user(permission.getUser() != null ? new UserResponse().castToResponse(permission.getUser()) : null)
                .isAccepted(permission.isAccepted())
                .isRejected(permission.isRejected())
                .build();
    }
}
