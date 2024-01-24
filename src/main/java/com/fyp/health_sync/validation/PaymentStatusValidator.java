package com.fyp.health_sync.validation;


import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PaymentStatusValidatorImpl.class)
public @interface PaymentStatusValidator {

    String message() default "Payment Status must be PENDING, SUCCESS or FAILED";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};




}
