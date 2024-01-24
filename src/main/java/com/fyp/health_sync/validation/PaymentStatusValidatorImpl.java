package com.fyp.health_sync.validation;

import jakarta.validation.ConstraintValidator;

import java.util.Arrays;
import java.util.List;

public class PaymentStatusValidatorImpl implements ConstraintValidator<PaymentStatusValidator,String> {
    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        List<String> paymentStatus = Arrays.asList("PENDING", "SUCCESS", "FAILED");
        return paymentStatus.contains(value);
    }
}
