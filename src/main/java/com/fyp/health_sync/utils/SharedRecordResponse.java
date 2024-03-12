package com.fyp.health_sync.utils;


import com.fyp.health_sync.entity.ShareMedicalRecords;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharedRecordResponse {

    private UUID id;
    private UserResponse user;
    private DoctorResponse doctor;
    private RecordResponse medicalRecords;
    private boolean isAccepted;
    private boolean isRejected;
    private boolean isExpired;

    public SharedRecordResponse castToResponse(ShareMedicalRecords shareMedicalRecords) {
        return SharedRecordResponse.builder()
                .id(shareMedicalRecords.getId())
                .user(shareMedicalRecords.getUser() == null ? null : new UserResponse().castToResponse(shareMedicalRecords.getUser()))
                .doctor(shareMedicalRecords.getDoctor() == null ? null : new DoctorResponse().castToResponse(shareMedicalRecords.getDoctor()))
                .isAccepted(shareMedicalRecords.isAccepted())
                .isRejected(shareMedicalRecords.isRejected())
                .isExpired(shareMedicalRecords.isExpired())
                .build();
    }
}
