package com.fyp.health_sync.controller;


import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getPayments(@RequestParam(required = true, defaultValue = "ALL") String sort) throws ForbiddenException, BadRequestException {
        return paymentService.getMyPayments(sort);
    }
}
