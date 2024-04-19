package com.fyp.health_sync.service_test;
import com.fyp.health_sync.dtos.TakeAppointmentDto;
import com.fyp.health_sync.entity.Appointments;
import com.fyp.health_sync.entity.Slots;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.PaymentStatus;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.*;
import com.fyp.health_sync.repository.SlotRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AppointmentServiceTest {

    @Mock
    private AppointmentRepo appointmentRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private SlotRepo slotRepo;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateAppointment_Success() throws BadRequestException, InternalServerErrorException {
        // Mocking
        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(auth.getName()).thenReturn("897chemical@awgarstone.com");
        when(userRepo.findByEmail(anyString())).thenReturn(new Users());
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(Users.builder().id(UUID.randomUUID()).status(UserStatus.ACTIVE).build()));
        when(slotRepo.findById(any(UUID.class))).thenReturn(Optional.of(Slots.builder().id(UUID.randomUUID()).doctor(new Users()).slotDateTime(LocalDateTime.now()).isBooked(false) .endTime(LocalDateTime.now().plusMinutes(30)).build()));

        // Test
        TakeAppointmentDto appointmentDto = new TakeAppointmentDto();
        ResponseEntity<?> response = appointmentService.createAppointment(appointmentDto);

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(appointmentRepo, times(1)).save(any(Appointments.class));
        verify(slotRepo, times(1)).save(any(Slots.class));
    }

    @Test
    public void testCancelAppointment_Success() throws BadRequestException, ForbiddenException, InternalServerErrorException {
        // Mocking
        when(userRepo.findByEmail(anyString())).thenReturn(new Users());
        when(appointmentRepo.findById(any(UUID.class))).thenReturn(Optional.of(new Appointments()));

        // Test
        ResponseEntity<?> response = appointmentService.cancelAppointment(UUID.randomUUID());

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(slotRepo, times(1)).save(any(Slots.class));
        verify(appointmentRepo, times(1)).delete(any(Appointments.class));
    }

    @Test
    public void testCancelAppointment_PaymentSuccess() {
        // Mocking
        Appointments appointment = new Appointments();
        appointment.setPaymentStatus(PaymentStatus.SUCCESS);
        when(userRepo.findByEmail(anyString())).thenReturn(new Users());
        when(appointmentRepo.findById(any(UUID.class))).thenReturn(Optional.of(appointment));

        // Test and Verify
        assertThrows(BadRequestException.class, () -> appointmentService.cancelAppointment(UUID.randomUUID()));
        verify(slotRepo, never()).save(any(Slots.class));
        verify(appointmentRepo, never()).delete(any(Appointments.class));
    }

    // Add more tests for other methods as needed
}
