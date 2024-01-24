package com.fyp.health_sync.validation;

import jakarta.validation.ConstraintValidator;

import java.util.List;

public class MedicalRecordTypeValidatorImpl implements ConstraintValidator<MedicalRecordTypeValidator,String> {
    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        List<String> MedicalRecordTypes = List.of("IMAGE", "DOCUMENT", "TEXT");

        return MedicalRecordTypes.contains(value);
    }
}
