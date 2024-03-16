package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Payment;
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
public class PaymentResponse {

    private UUID id;
    private Integer amount;
    private LocalDateTime createdAt;
    private String khaltiMobile;
    private String khaltiToken;
    private String paymentType;
    private UserResponse user;
    private DoctorResponse doctor;
    private AppointmentResponse appointment;
    private String transactionId;

    public PaymentResponse castToResponse(Payment payment) {
      return PaymentResponse.builder()
              .id(payment.getId())
              .amount(payment.getAmount())
              .createdAt(payment.getCreatedAt())
              .khaltiMobile(payment.getKhaltiMobile())
              .khaltiToken(payment.getKhaltiToken())
              .paymentType(payment.getPaymentType())
              .user(payment.getUser() == null ? null : new UserResponse().castToResponse(payment.getUser()))
              .doctor(payment.getDoctor() == null ? null : new DoctorResponse().castToResponse(payment.getDoctor()))
                .appointment(payment.getAppointment() == null ? null : new AppointmentResponse().castToResponse(payment.getAppointment()))
                .transactionId(payment.getTransactionId())
              .build();
    }
}
