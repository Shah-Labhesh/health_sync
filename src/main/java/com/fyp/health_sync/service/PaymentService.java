package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.Payment;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.PaymentRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;


    public ResponseEntity<?> getMyPayments(String sort) throws BadRequestException, ForbiddenException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        List<Payment> payments = new ArrayList<>();
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        if (user.getRole() == UserRole.DOCTOR) {
            payments = paymentRepo.findAllByDoctor(user);
        } else if (user.getRole() == UserRole.USER) {
            payments = paymentRepo.findAllByUser(user);
        }else{
            throw new ForbiddenException("You are not authorized to view payments");
        }

        switch (sort) {

            case "PENDING" -> payments.forEach(payment -> {
                if (payment.getTransactionId() == null) {
                    paymentResponses.add(new PaymentResponse().castToResponse(payment));
                }
                
            });
            case "SETTLED" -> payments.forEach(payment -> {
                if (payment.getTransactionId() != null) {
                    paymentResponses.add(new PaymentResponse().castToResponse(payment));
                }
            });
            case "ALL" -> payments.forEach(payment -> {
                paymentResponses.add(new PaymentResponse().castToResponse(payment));
            });
            default -> throw new BadRequestException("Invalid sort parameter");
        }
        return ResponseEntity.ok(paymentResponses);

    }

    // get all payments for admin
    public ResponseEntity<?> getAllPayments() throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("User not found");
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You are not authorized to view payments");
        }
        List<Payment> payments = paymentRepo.findAll();
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        payments.forEach(payment -> {
            paymentResponses.add(new PaymentResponse().castToResponse(payment));
        });
        Map<String, Object> response = new HashMap<>();
            response.put("totalSettlements", paymentRepo.countTotalAmount());
            response.put("pendingAmount", paymentRepo.countPendingAmount());
            response.put("transaction", paymentResponses);
        return ResponseEntity.ok(response);
        } catch(BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch(ForbiddenException e){
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
