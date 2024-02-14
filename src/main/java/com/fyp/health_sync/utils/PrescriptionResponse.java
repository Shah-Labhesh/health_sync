package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Prescriptions;
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
public class PrescriptionResponse {
    private UUID id;

    private String recordType;
    private String prescriptionType;
    private String prescription;
    private String prescriptionText;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private UserResponse user;
    private DoctorResponse doctor;

    public PrescriptionResponse castToResponse(Prescriptions prescriptions){
        return PrescriptionResponse.builder()
                .id(prescriptions.getId())
                .recordType(prescriptions.getRecordType())
                .prescription(prescriptions.getPrescription() != null ? "/files/prescription/"+prescriptions.getId() : null)
                .prescriptionText(prescriptions.getPrescriptionText())
                .createdAt(prescriptions.getCreatedAt())
                .deletedAt(prescriptions.getDeletedAt())
                .user(new UserResponse().castToResponse(prescriptions.getUser()))
                .doctor(new DoctorResponse().castToResponse(prescriptions.getDoctor()))
                .build();
    }
}
