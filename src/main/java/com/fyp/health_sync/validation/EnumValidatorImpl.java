package com.fyp.health_sync.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    private EnumValidator annotation;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Consider null as valid
        }

        Class<? extends Enum<?>> enumClass = annotation.enumClass();

        if (enumClass.isEnum()) {
            for (Enum<?> enumValue : enumClass.getEnumConstants()) {
                if (enumValue.name().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
