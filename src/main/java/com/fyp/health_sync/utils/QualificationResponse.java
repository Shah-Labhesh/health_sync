package com.fyp.health_sync.utils;

import java.util.UUID;

import com.fyp.health_sync.entity.Qualifications;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QualificationResponse {
    
    private UUID id;
    private String qualification;
    private String institute;
    private String passOutYear;
    private String certificate;
    private DoctorResponse doctor;

    public QualificationResponse castToResponse(Qualifications qualification) {
        if (qualification == null) {
            return null;
        }
        return QualificationResponse.builder()
                .id(qualification.getId())
                .qualification(qualification.getQualification())
                .institute(qualification.getInstitute())
                .passOutYear(qualification.getPassOutYear().toString())
                .certificate(qualification.getCertificate() != null ? "certificate/"+qualification.getId() : null)
                .doctor(qualification.getDoctor() != null ? new DoctorResponse().castToResponse(qualification.getDoctor()) : null)
                .build();
    }
}
