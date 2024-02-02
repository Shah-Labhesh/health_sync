package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecordResponse {

    private UUID id;
    private String recordType;
    private String record;
    private String recordCreatedDate;
    private boolean selfAdded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private UserResponse user;
    private DoctorResponse doctor;

    public RecordResponse castToResponse(MedicalRecords record){
        if (record == null) {
            return null;
        }
        if (recordType(record.getRecordType()) == RecordType.PDF) {
            return RecordResponse.builder()
                    .id(record.getId())
                    .recordType(record.getRecordType())
                    .record(record.getRecord() != null ? "/files/pdf-record/" + record.getId() : null)
                    .recordCreatedDate(record.getRecordCreatedDate())
                    .selfAdded(record.isSelfAdded())
                    .createdAt(record.getCreatedAt())
                    .updatedAt(record.getUpdatedAt())
                    .deletedAt(record.getDeletedAt())
                    .user(record.getUser() != null ? new UserResponse().castToResponse(record.getUser()) : null)
                    .doctor(record.getDoctor() != null ? new DoctorResponse().castToResponse(record.getDoctor()) : null)
                    .build();
        }
        return RecordResponse.builder()
                .id(record.getId())
                .recordType(record.getRecordType())
                .record(record.getRecord() != null ? "/files/image-record/"+ record.getId() : null)
                .recordCreatedDate(record.getRecordCreatedDate())
                .selfAdded(record.isSelfAdded())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .deletedAt(record.getDeletedAt())
                .user(record.getUser() != null ? new UserResponse().castToResponse(record.getUser()) : null)
                .doctor(record.getDoctor() != null ? new DoctorResponse().castToResponse(record.getDoctor()) : null)
                .build();
    }

    public RecordType recordType(String recordType) {
        if (Objects.equals(recordType, "PDF")) {
            return RecordType.PDF;
        }
        return RecordType.IMAGE;
    }
}
