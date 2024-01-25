package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.RecordType;
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
public class RecordResponse {

    private UUID id;
    private String recordType;
    private String record;
    private String recordText;
    private boolean selfAdded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Users user;
    private Users doctor;

    public RecordResponse castToResponse(MedicalRecords record){
        if (record == null) {
            return null;
        }
        if (record.getRecordType() == RecordType.DOCUMENT) {
            return RecordResponse.builder()
                    .id(record.getId())
                    .recordType(record.getRecordType().toString())
                    .record(record.getRecord() != null ? "pdf-record/" + record.getId() : null)
                    .recordText(record.getRecordText())
                    .selfAdded(record.isSelfAdded())
                    .createdAt(record.getCreatedAt())
                    .updatedAt(record.getUpdatedAt())
                    .deletedAt(record.getDeletedAt())
                    .user(record.getUser())
                    .doctor(record.getDoctor())
                    .build();
        }
        return RecordResponse.builder()
                .id(record.getId())
                .recordType(record.getRecordType().toString())
                .record(record.getRecord() != null ? "image-record/"+ record.getId() : null)
                .recordText(record.getRecordText())
                .selfAdded(record.isSelfAdded())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .deletedAt(record.getDeletedAt())
                .user(record.getUser())
                .doctor(record.getDoctor())
                .build();
    }
}
