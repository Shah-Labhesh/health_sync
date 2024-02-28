package com.fyp.health_sync.utils;

import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Payment;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.PaymentStatus;
import jakarta.persistence.*;
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
public class AppointmentResponse {
    private UUID id;
    private String appointmentId;
    private Boolean isExpired;
    private LocalDateTime reminderTime;
    private Integer appointmentFee;
    private Integer platformCost;
    private Integer totalFee;
    private DoctorResponse doctor;

    private UserResponse user;
    private String appointmentType;
    private SlotsResponse slot;
    private String notes;
    private String payment;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;


    public AppointmentResponse castToResponse(Appointments appointments){
        return AppointmentResponse.builder()
                .id(appointments.getId())
                .doctor(new DoctorResponse().castToResponse(appointments.getDoctor()))
                .user(new UserResponse().castToResponse(appointments.getUser()))
                .appointmentType(appointments.getAppointmentType())
                .slot(new SlotsResponse().castToResponse(appointments.getSlot()))
                .appointmentFee(appointments.getAppointmentFee())
                .appointmentId(appointments.getAppointmentId())
                .platformCost(appointments.getPlatformCost())
                .totalFee(appointments.getTotalFee())
                .reminderTime(appointments.getReminderTime())
                .isExpired(appointments.getIsExpired())
                .notes(appointments.getNotes())
                .payment(appointments.getPayment() != null ? appointments.getPayment().getId().toString() : null)
                .paymentStatus(appointments.getPaymentStatus())
                .createdAt(appointments.getCreatedAt())
                .updatedAt(appointments.getUpdatedAt())
                .deletedAt(appointments.getDeletedAt())
                .build();
    }
}
