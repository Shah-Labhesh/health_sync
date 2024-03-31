package com.fyp.health_sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyp.health_sync.dtos.ConfirmRequestDto;
import com.fyp.health_sync.dtos.KhaltiRequestDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.FirebaseToken;
import com.fyp.health_sync.entity.Payment;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.NotificationType;
import com.fyp.health_sync.enums.PaymentStatus;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.AppointmentRepo;
import com.fyp.health_sync.repository.FirebaseTokenRepo;
import com.fyp.health_sync.repository.PaymentRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.SuccessResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import lombok.RequiredArgsConstructor;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KhaltiService {
    private static final String INITIATE_URL = "https://khalti.com/api/v2/payment/initiate/";
    private static final String CONFIRM_URL = "https://khalti.com/api/v2/payment/confirm/";
    private static final String VERIFY_URL="https://khalti.com/api/v2/payment/verify/";

    @Value("test_public_key_1bdb1266d642404d9790da96355f3a73")
    private String TEST_PUBLIC_KEY;

    @Value("test_secret_key_590996dcc7dc4b5ebd5b314512ecab0e")
    private String TEST_SECRET_KEY;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;
    private final PaymentRepo paymentRepo;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final FirebaseTokenRepo firebaseTokenRepo;

    public ResponseEntity<?> initiateTransaction(KhaltiRequestDto khaltiRequest)
            throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("Only users can make payments");
            }

            Appointments appointment = appointmentRepo.findById(khaltiRequest.getProduct_identity())
                    .orElseThrow(() -> new BadRequestException("Appointment not found"));
            if (appointment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw new BadRequestException("Payment already made for this appointment");
            }
            if (appointment.getUser().getId() != user.getId()) {
                throw new ForbiddenException("You are not authorized to make payment for this appointment");
            }
            Payment payment = paymentRepo.findByAppointment(appointment);
            if (payment != null){
                throw new BadRequestException("Payment already done for this appointment");
            }

            khaltiRequest.setPublic_key(TEST_PUBLIC_KEY);
            khaltiRequest.setAmount(1000);
            khaltiRequest.setProduct_name("Health Sync Appointment");
            Map<String, Object> mapResponse = new HashMap<>();
            Object response = restTemplate.postForObject(INITIATE_URL, khaltiRequest, Object.class);
            if (response == null) {
                throw new InternalServerErrorException("Khalti Server Error");
            }
            String jsonResponse = objectMapper.writeValueAsString(response);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            mapResponse.put("khalti_token", jsonNode.get("token").asText());
            

            return ResponseEntity.created(null).body(mapResponse);
        } 
        catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> confirmTransaction(ConfirmRequestDto confirmKhaltiRequest)
            throws ForbiddenException, BadRequestException, InternalServerErrorException {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            if (user.getRole() != UserRole.USER) {
                throw new ForbiddenException("Only users can make payments");
            }

            Appointments appointment = appointmentRepo.findById(confirmKhaltiRequest.getAppointmentId())
                    .orElseThrow(() -> new BadRequestException("Appointment not found"));
            if ( paymentRepo.findByAppointment(appointment) != null){
                throw new BadRequestException("Payment already made for this appointment");
            }
            if (appointment.getUser().getId() != user.getId()) {
                throw new ForbiddenException("You are not authorized to make payment for this appointment");
            }
            confirmKhaltiRequest.setPublic_key(TEST_PUBLIC_KEY);
            Object response = restTemplate.postForObject(CONFIRM_URL, confirmKhaltiRequest, Object.class);
            String jsonResponse = objectMapper.writeValueAsString(response);

            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            JsonNode token = jsonNode.get("token");
            JsonNode amount = jsonNode.get("amount");

            String idx = verifyTransaction(token.asText());

            if (idx == null){
                throw new BadRequestException("Transaction not verified");
            }

            appointment.setPaymentStatus(PaymentStatus.SUCCESS);
            Payment payment = Payment.builder()
                    .amount(appointment.getAppointmentFee())
                    .createdAt(LocalDateTime.now())
                    .khaltiMobile(confirmKhaltiRequest.getMobile())
                    .khaltiToken(token.asText())
                    .paymentType("Khalti")
                    .user(user)
                    .doctor(appointment.getDoctor())
                    .appointment(appointment)
                    .transactionId(idx)
                    .build();

            appointmentRepo.save(appointment);
            paymentRepo.save(payment);

            notificationService.sendNotification(payment.getId(),
                    appointment.getUser().getName() + " made payment of Rs." + appointment.getTotalFee()
                            + " for appointment " + appointment.getAppointmentId(),
                    NotificationType.PAYMENT, appointment.getDoctor().getId());
            notificationService.sendNotification(
                    payment.getId(), "Payment successful for appointment with Dr. "
                            + appointment.getDoctor().getName() + " of Rs." + appointment.getTotalFee(),
                    NotificationType.PAYMENT, appointment.getUser().getId());
            for (FirebaseToken fToken : firebaseTokenRepo.findAllByUser(appointment.getUser())) {
                pushNotificationService.sendNotification(
                        "Appointment Booked", "Payment successful for appointment with Dr. "
                                + appointment.getDoctor().getName() + " of Rs." + appointment.getTotalFee(),
                        fToken.getToken());
            }
            for (FirebaseToken fToken : firebaseTokenRepo.findAllByUser(appointment.getDoctor())) {
                pushNotificationService.sendNotification(
                        "New Appointment", appointment.getUser().getName() + " made payment of Rs."
                                + appointment.getTotalFee() + " for appointment " + appointment.getAppointmentId(),
                        fToken.getToken());
            }
            return ResponseEntity.created(null).body(new SuccessResponse("Payment confirmed successfully"));
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public String verifyTransaction(String token) throws JsonProcessingException, JSONException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Key "+TEST_SECRET_KEY);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("token", token);
        map.add("amount", "1000");

        HttpEntity<Object> requestEntity =
                new HttpEntity<>(map, headers);

        Object response = restTemplate.exchange(VERIFY_URL,HttpMethod.POST, requestEntity, Object.class);
        String jsonResponse = objectMapper.writeValueAsString(response);
        System.out.println(jsonResponse);

        JSONObject jsonObject=new JSONObject(jsonResponse);
        JSONObject body = (JSONObject) jsonObject.get("body");

        return (String) body.get("idx");

        

    }

}