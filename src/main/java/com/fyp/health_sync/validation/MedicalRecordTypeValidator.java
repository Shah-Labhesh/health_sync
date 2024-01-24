package com.fyp.health_sync.validation;


import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = MedicalRecordTypeValidatorImpl.class)
public @interface MedicalRecordTypeValidator {

    public String message() default "Medical Record Type must be one of the following: IMAGE, DOCUMENT, TEXT";

    public Class<?>[] groups() default {};

    public Class<? extends jakarta.validation.Payload>[] payload() default {};

}
