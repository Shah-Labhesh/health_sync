package com.fyp.health_sync.service_test;
import com.fyp.health_sync.entity.DataRemovalRequest;
import com.fyp.health_sync.entity.MedicalRecords;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.*;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.service.*;
import com.fyp.health_sync.utils.DoctorResponse;
import com.fyp.health_sync.utils.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.*;


public class AdminServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private MailService mailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FirebaseTokenRepo firebaseTokenRepo;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private AdminService adminService;

    @Mock
    private MedicalRecordRepo medicalRecordsRepo;

    @Mock
    private DataRemovalRequestRepo dataRemovalRequestRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testUpdateApprovedStatus_DoctorNotFound() {
        UUID id = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> {
            adminService.updateApprovedStatus(id, true, "Approved");
        });

    }

    @Test
    public void testUpdateApprovedStatus_AccountNotActive() {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.INACTIVE); // Assuming INACTIVE is the status for not active
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        assertThrows(BadRequestException.class, () -> {
            adminService.updateApprovedStatus(id, true, "Approved");
        });
    }

    @Test
    public void testUpdateApprovedStatus_RejectionMessageRequired() {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.ACTIVE);
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        assertThrows(BadRequestException.class, () -> {
            adminService.updateApprovedStatus(id, false, null);
        });
    }

    @Test
    public void testUpdateApprovedStatus_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.ACTIVE);
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        ResponseEntity<?> response = adminService.updateApprovedStatus(id, true, "Approved");

        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("Approval Status updated successfully", response.getBody().getMessage());
//        assertTrue(doctor.isApproved());
        verify(userRepo, times(1)).save(doctor);
    }

    @Test
    public void testUpdatePopularStatus_DoctorNotFound() {
        UUID id = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> {
            adminService.updatePopularStatus(id, true);
        });
    }

    @Test
    public void testUpdatePopularStatus_AccountNotActive() {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.INACTIVE); // Assuming INACTIVE is the status for not active
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        assertThrows(BadRequestException.class, () -> {
            adminService.updatePopularStatus(id, true);
        });
    }

    @Test
    public void testUpdatePopularStatus_DoctorNotApproved() {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.ACTIVE);
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        assertThrows(BadRequestException.class, () -> {
            adminService.updatePopularStatus(id, true);
        });
    }

    @Test
    public void testUpdatePopularStatus_StatusAlreadyUpdated() {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setApproved(true);
        doctor.setPopular(true); // Assuming the initial status is true
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        assertThrows(BadRequestException.class, () -> {
            adminService.updatePopularStatus(id, true);
        });
    }

    @Test
    public void testUpdatePopularStatus_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users doctor = new Users();
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setApproved(true);
        when(userRepo.findById(id)).thenReturn(Optional.of(doctor));

        ResponseEntity<?> response = adminService.updatePopularStatus(id, true);

        assertEquals(200, response.getStatusCodeValue());
        verify(userRepo, times(1)).save(doctor);
    }


    @Test
    public void testChangeAccountStatus_UserNotFound() {
        UUID id = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> {
            adminService.changeAccountStatus(id, UserStatus.ACTIVE);
        });
    }

    @Test
    public void testChangeAccountStatus_UserAlreadyDeleted() {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.DELETED); // Assuming DELETED is the status for already deleted
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> {
            adminService.changeAccountStatus(id, UserStatus.DELETED);
        });
    }

    @Test
    public void testChangeAccountStatus_StatusDeleted_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = adminService.changeAccountStatus(id, UserStatus.DELETED);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(user.getDeletedAt());
        verify(userRepo, times(1)).save(user);
    }

    @Test
    public void testChangeAccountStatus_StatusActive_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.DELETED); // Set status to DELETED initially
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = adminService.changeAccountStatus(id, UserStatus.ACTIVE);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNull(user.getDeletedAt()); // DeletedAt should be null for non-DELETED status
        verify(userRepo, times(1)).save(user);
    }

    @Test
    public void testDeleteUser_UserNotFound() {
        UUID id = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> {
            adminService.deleteUser(id);
        });
    }

    @Test
    public void testDeleteUser_UserNotInTrash() {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.ACTIVE); // Assuming ACTIVE status
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> {
            adminService.deleteUser(id);
        });
    }

    @Test
    public void testDeleteUser_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.DELETED); // Assuming DELETED status
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = adminService.deleteUser(id);

        assertEquals(200, response.getStatusCodeValue());
        verify(userRepo, times(1)).delete(user);
    }

    @Test
    public void testRestoreUser_UserNotFound() {
        UUID id = UUID.randomUUID();
        assertThrows(BadRequestException.class, () -> {
            adminService.restoreUser(id);
        });
    }

    @Test
    public void testRestoreUser_UserNotInTrash() {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.ACTIVE); // Assuming ACTIVE status
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> {
            adminService.restoreUser(id);
        });
    }

    @Test
    public void testRestoreUser_Success() throws BadRequestException, InternalServerErrorException {
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setStatus(UserStatus.DELETED); // Assuming DELETED status
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = adminService.restoreUser(id);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(user.getDeletedAt()); // DeletedAt should be null after restoration
        assertEquals(UserStatus.ACTIVE, user.getStatus()); // Status should be ACTIVE after restoration
        verify(userRepo, times(1)).save(user);
    }

    @Test
    public void testGetUsersByStatus_Success() throws InternalServerErrorException {
        // Mocking the repository call
        when(userRepo.findAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER)).thenReturn(getMockUsers());

        // Call the method to test
        List<UserResponse> response = adminService.getUsersByStatus(UserStatus.ACTIVE);

        // Verify the response
        assertNotNull(response);
        assertEquals(2, response.size()); // Assuming there are 2 users with status ACTIVE and role USER

        // Add more assertions as per your actual implementation and expectations
    }

    @Test
    public void testGetDoctorsByStatus_Success() throws InternalServerErrorException {
        // Mocking the repository call
        when(userRepo.findAllByStatusAndRoleAndApproved(UserStatus.ACTIVE, UserRole.DOCTOR, true)).thenReturn(getMockDoctors());

        // Call the method to test
        List<DoctorResponse> response = adminService.getDoctorsByStatus(UserStatus.ACTIVE);

        // Verify the response
        assertNotNull(response);
        assertEquals(2, response.size()); // Assuming there are 2 doctors with status ACTIVE and approved

        // Add more assertions as per your actual implementation and expectations
    }

    @Test
    public void testGetAllUnapprovedDoctors_Success() throws InternalServerErrorException {
        // Mocking the repository call
        when(userRepo.findAllByApprovedFalseAndRole(UserRole.DOCTOR)).thenReturn(getMockUnapprovedDoctors());

        // Call the method to test
        List<DoctorResponse> response = adminService.getAllUnapprovedDoctors();

        // Verify the response
        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    public void testGetDashboardData_Success() throws InternalServerErrorException {
        // Mocking the repository calls
        when(userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER)).thenReturn(10);
        when(userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.DOCTOR)).thenReturn(5);
        when(userRepo.countAllByApprovedFalseAndRole(UserRole.DOCTOR)).thenReturn(3);
        when(userRepo.findAllByApprovedFalseAndRole(UserRole.DOCTOR)).thenReturn(getMockUnapprovedDoctors());

        // Call the method to test
        ResponseEntity<?> responseEntity = adminService.getDashboardData();

        // Verify the response
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

    }

    @Test
    public void testManageUser_Success() throws InternalServerErrorException {
        // Mocking the repository calls
        when(userRepo.countAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER)).thenReturn(10);
        when(userRepo.countAllByStatusAndRole(UserStatus.SUSPENDED, UserRole.USER)).thenReturn(2);
        when(userRepo.countAllByStatusAndRole(UserStatus.DELETED, UserRole.USER)).thenReturn(3);
        when(userRepo.findAllByStatusAndRole(UserStatus.ACTIVE, UserRole.USER)).thenReturn(getMockUsers());

        // Call the method to test
        ResponseEntity<?> responseEntity = adminService.manageUser(UserStatus.ACTIVE);

        // Verify the response
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

    }

    // Helper method to generate mock users
    private List<Users> getMockUsers() {
        List<Users> users = new ArrayList<>();
        // Add mock users with status ACTIVE and role USER
        users.add(Users.builder().id(UUID.randomUUID()).isVerified(true).status(UserStatus.ACTIVE).role(UserRole.USER).createdAt(LocalDateTime.now()).build());
        users.add(Users.builder().id(UUID.randomUUID()).isVerified(true).status(UserStatus.ACTIVE).role(UserRole.USER).createdAt(LocalDateTime.now()).build());
        return users;
    }

    // Helper method to generate mock doctors
    private List<Users> getMockDoctors() {
        List<Users> doctors = new ArrayList<>();
        // Add mock users with status ACTIVE, role DOCTOR, and approved
        doctors.add(Users.builder().id(UUID.randomUUID()).isVerified(true).approved(true).status(UserStatus.ACTIVE).createdAt(LocalDateTime.now()).role(UserRole.DOCTOR).build());
        doctors.add(Users.builder().id(UUID.randomUUID()).isVerified(true).approved(true).status(UserStatus.ACTIVE).createdAt(LocalDateTime.now()).role(UserRole.DOCTOR).build());
        return doctors;
    }

    // Helper method to generate mock unapproved doctors
    private List<Users> getMockUnapprovedDoctors() {
        List<Users> unapprovedDoctors = new ArrayList<>();
        // Add mock users with status ACTIVE, role DOCTOR, and not approved
        unapprovedDoctors.add(Users.builder().id(UUID.randomUUID()).isVerified(true).approved(false).role(UserRole.DOCTOR).createdAt(LocalDateTime.now()).build());
        unapprovedDoctors.add(Users.builder().id(UUID.randomUUID()).isVerified(true).approved(false).role(UserRole.DOCTOR).createdAt(LocalDateTime.now()).build());
        return unapprovedDoctors;
    }

    @Test
    public void testGetAllDataRemovalRequests_Success() throws InternalServerErrorException {
        // Mocking the repository call
        when(dataRemovalRequestRepo.findAll()).thenReturn(Collections.emptyList());

        // Call the method to test
        ResponseEntity<?> responseEntity = adminService.getAllDataRemovalRequests();

        // Verify the response
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
    }

    @Test
    public void testAcceptDataRemovalRequest_Success() throws BadRequestException, InternalServerErrorException {
        // Mocking the repository call
        UUID requestId = UUID.randomUUID();
        DataRemovalRequest request = new DataRemovalRequest();
        request.setType("MEDICAL_RECORDS_DELETION");
        when(dataRemovalRequestRepo.findById(requestId)).thenReturn(Optional.of(request));
        MedicalRecords medicalRecords = new MedicalRecords();
        when(medicalRecordsRepo.findAllByUser(request.getUser())).thenReturn(Collections.singletonList(medicalRecords));
        // Call the method to test
        ResponseEntity<?> responseEntity = adminService.acceptDataRemovalRequest(requestId);

        // Verify the response
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(request.isAccepted());
        verify(dataRemovalRequestRepo, times(1)).save(request);
    }

    @Test
    public void testRejectDataRemovalRequest_Success() throws BadRequestException, InternalServerErrorException {
        // Mocking the repository call
        UUID requestId = UUID.randomUUID();
        DataRemovalRequest request = new DataRemovalRequest();
        when(dataRemovalRequestRepo.findById(requestId)).thenReturn(Optional.of(request));

        // Call the method to test
        ResponseEntity<?> responseEntity = adminService.rejectDataRemovalRequest(requestId);

        // Verify the response
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(request.isRejected());
        verify(dataRemovalRequestRepo, times(1)).save(request);
    }
}
