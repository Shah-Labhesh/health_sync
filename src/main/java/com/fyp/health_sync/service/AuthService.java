package com.fyp.health_sync.service;

import com.fyp.health_sync.config.CustomUserDetails;
import com.fyp.health_sync.config.JwtHelper;
import com.fyp.health_sync.dtos.*;
import com.fyp.health_sync.entity.Doctors;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.OtpType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.DoctorRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.LoginResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService implements UserDetailsService {
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;
    private final JwtHelper jwtHelper;
    private final MailService mailService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Users user = userRepo.findByEmail(username);
        Doctors doctor = doctorRepo.findByEmail(username);


        if (user != null) {

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UsernameNotFoundException("User not found.");
            }
            return new CustomUserDetails(user.getEmail(), user.getPassword(), user.getRoles());

        } else if (doctor != null) {
            if (doctor.getAccountStatus() != UserStatus.ACTIVE) {
                throw new UsernameNotFoundException("User not found.");
            }

            return new CustomUserDetails(doctor.getEmail(), doctor.getPassword(), doctor.getRoles());
        } else {
            throw new UsernameNotFoundException("User not found.");

        }

    }

    private Boolean validateAuthType(AuthType authType) {
        return authType == AuthType.Google || authType == AuthType.Twitter;
    }

    public void registerUser(UserCreateDto userDetils) {
        Users user = userRepo.findByEmail(userDetils.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.DELETED) {
                throw new DataIntegrityViolationException("Email is in trash. Please contact admin");
            } else {

                throw new DataIntegrityViolationException("User already exists");
            }


        }
        Doctors doctor = doctorRepo.findByEmail(userDetils.getEmail());
        if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.DELETED) {
                throw new DataIntegrityViolationException("Email is in trash. Please contact admin");
            } else {
                throw new DataIntegrityViolationException("Doctor already exists");
            }
        }


        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        userDetils.setPassword(bCrypt.encode(userDetils.getPassword()));
        Users user2 = Users.builder()
                .id(UUID.randomUUID())
                .name(userDetils.getName())
                .email(userDetils.getEmail())
                .password(userDetils.getPassword())
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .authType(AuthType.Traditional)
                .build();
        userRepo.save(user2);

    }

    public ResponseEntity<?> registerDoctor(RegisterDoctorDto doctorDetail) {
        Users user = userRepo.findByEmail(doctorDetail.getEmail());
        if (user != null ) {
            if (user.getStatus() == UserStatus.DELETED) {
                throw new DataIntegrityViolationException("Email is in trash. Please contact admin");
            } else {

                throw new DataIntegrityViolationException("User already exists");
            }
        }

        Doctors doctor = doctorRepo.findByEmail(doctorDetail.getEmail());
        if (doctor != null ) {
            if (doctor.getAccountStatus() == UserStatus.DELETED) {
                throw new DataIntegrityViolationException("Email is in trash. Please contact admin");
            } else {
                throw new DataIntegrityViolationException("Doctor already exists");
            }
        }

        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        doctorDetail.setPassword(bCrypt.encode(doctorDetail.getPassword()));
        Doctors doc = Doctors.builder().name(doctorDetail.getName())
                .email(doctorDetail.getEmail())
                .password(doctorDetail.getPassword())
                .role(UserRole.DOCTOR)
                .accountStatus(UserStatus.ACTIVE)
                .authType(AuthType.Traditional)
                .approved(false)
                .isPopular(false)
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .build();
        doctorRepo.save(doc);
        return ResponseEntity.created(null).body(doctorRepo.save(doc));

    }

    public ResponseEntity<?> performLogin(LoginDto credentials) throws BadRequestException {
        Users user = userRepo.findByEmail(credentials.getEmail());
        Doctors doctor = doctorRepo.findByEmail(credentials.getEmail());
        if (user != null) {

            if (user.getStatus() != UserStatus.DELETED) {
                if (validateAuthType(user.getAuthType())) {
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }else{
                    return loginUser(credentials);
                }

            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() != UserStatus.DELETED) {
                if (validateAuthType(doctor.getAuthType())) {
                    throw new BadRequestException("Please login with " + doctor.getAuthType());
                }else{
                    return loginDoctor(credentials);
                }
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    private ResponseEntity<?> loginUser(LoginDto userDetils) throws BadRequestException {
        Users user = userRepo.findByEmail(userDetils.getEmail());
        if (user == null) {
            throw new BadRequestException("Invalid Credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User is " + user.getStatus());
        }
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        if (bCrypt.matches(userDetils.getPassword(), user.getPassword())) {
                if(user.getIsVerified()){
//             get  user role
            String role = null;
            for (GrantedAuthority userRole : user.getRoles()) {
                role = userRole.getAuthority();
            }
            UserDetails userDetails = loadUserByUsername(user.getEmail());
            String token = jwtHelper.generateToken(userDetails);
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setVerified(user.getIsVerified());
            response.setAuthType(user.getAuthType());
            response.setStatus(user.getStatus());
            response.setRole(role);
            response.setMessage("User Login successfully");
            return ResponseEntity.created(null).body(response);
                }
                else{
                    throw new BadRequestException("Please verify your email before login.");
                }

        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    private ResponseEntity<?> loginDoctor(LoginDto user) throws BadRequestException {
        Doctors doctor = doctorRepo.findByEmail(user.getEmail());
        if (doctor == null) {
            throw new BadRequestException("Invalid Credentials");
        }
        if (doctor.getAccountStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Doctor is " + doctor.getAccountStatus());
        }
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        if (bCrypt.matches(user.getPassword(), doctor.getPassword())) {
                if(doctor.getIsVerified()){
            String role = null;
            for (GrantedAuthority userRole : doctor.getRoles()) {
                role = userRole.getAuthority();
            }
            UserDetails userDetails = loadUserByUsername(doctor.getEmail());
            String token = jwtHelper.generateToken(userDetails);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setVerified(doctor.getIsVerified());
            response.setAuthType(doctor.getAuthType());
            response.setStatus(doctor.getAccountStatus());
            response.setRole(role);
            response.setMessage("User Login successfully");
            return ResponseEntity.created(null).body(response);
                }
                else{
                    throw new BadRequestException("Please verify your email before login.");
                }

        } else {
            throw new BadRequestException("Invalid Credentials");

        }

    }

    public ResponseEntity<?> initiateEmailVerification(String email) throws BadRequestException {
        SuccessResponse response = new SuccessResponse();
        Users user = userRepo.findByEmail(email);
        Doctors doctor = doctorRepo.findByEmail(email);
        if (user != null) {
            if (user.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }

            if (user.getStatus() == UserStatus.ACTIVE) {
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Email Verification", "verify your email");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                mailService.sendEmail(doctor.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Email Verification", "verify your email");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    public ResponseEntity<?> resendEmailVerification(String email) throws BadRequestException {
        SuccessResponse response = new SuccessResponse();
        Users user = userRepo.findByEmail(email);
        Doctors doctor = doctorRepo.findByEmail(email);

        if (user != null) {
            if (user.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }
            if (user.getStatus() != UserStatus.DELETED) {
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Resend Email Verification", "verify your email");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }
            if (doctor.getAccountStatus() != UserStatus.DELETED) {
                mailService.sendEmail(doctor.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Resend Email Verification", "verify your email");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }


    public ResponseEntity<?> verifyEmail(EmailVerificationDto emailVerificationDto) throws BadRequestException {
        Users user = userRepo.findByEmail(emailVerificationDto.getEmail());
        Doctors doctor = doctorRepo.findByEmail(emailVerificationDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (user.getIsVerified()) {
                    throw new BadRequestException("Email already verified");
                } else {
                    if (otpService.validateOtp(emailVerificationDto.getEmail(), emailVerificationDto.getOtp(), OtpType.EMAIL_VERIFICATION)) {
                        user.setIsVerified(true);
                        userRepo.save(user);
                        SuccessResponse response = new SuccessResponse();
                        response.setMessage("Email verified successfully");
                        return ResponseEntity.created(null).body(response);
                    } else {
                        throw new BadRequestException("Invalid OTP");
                    }
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                if (doctor.getIsVerified()) {
                    throw new BadRequestException("Email already verified");
                } else {

                    if (otpService.validateOtp(emailVerificationDto.getEmail(), emailVerificationDto.getOtp(), OtpType.EMAIL_VERIFICATION)) {
                        doctor.setIsVerified(true);
                        doctorRepo.save(doctor);
                        SuccessResponse response = new SuccessResponse();
                        response.setMessage("Email verified successfully");
                        return ResponseEntity.created(null).body(response);
                    } else {
                        throw new BadRequestException("Invalid OTP");
                    }
                }
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Email not registered");
        }
    }

    public ResponseEntity<?> initiateForgotPassword(String email) throws BadRequestException {
        SuccessResponse response = new SuccessResponse();
        Users user = userRepo.findByEmail(email);
        Doctors doctor = doctorRepo.findByEmail(email);
        if (user != null) {

            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Password Reset", "reset your password");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(doctor.getAuthType())){
                    throw new BadRequestException("Please login with " + doctor.getAuthType());
                }
                mailService.sendEmail(doctor.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Password Reset", "reset your password");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    public ResponseEntity<?> resendForgotPassword(String email) throws BadRequestException {
        SuccessResponse response = new SuccessResponse();
        Users user = userRepo.findByEmail(email);
        Doctors doctor = doctorRepo.findByEmail(email);
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Resend Password Reset", "reset your password");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(doctor.getAuthType())){
                    throw new BadRequestException("Please login with " + doctor.getAuthType());
                }
                mailService.sendEmail(doctor.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Resend Password Reset", "reset your password");
                response.setMessage("OTP sent successfully");
                return ResponseEntity.created(null).body(response);
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    // verifyForgotPassword
    public ResponseEntity<?> verifyForgotPassword(VerifyForgotPasswordDto verifyForgotPasswordDto) throws BadRequestException {
        Users user = userRepo.findByEmail(verifyForgotPasswordDto.getEmail());
        Doctors doctor = doctorRepo.findByEmail(verifyForgotPasswordDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                if (otpService.validateOtp(verifyForgotPasswordDto.getEmail(), verifyForgotPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {


                    SuccessResponse response = new SuccessResponse();
                    response.setMessage("OTP verified successfully");
                    return ResponseEntity.created(null).body(response);
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(doctor.getAuthType())){
                    throw new BadRequestException("Please login with " + doctor.getAuthType());
                }
                if (otpService.validateOtp(verifyForgotPasswordDto.getEmail(), verifyForgotPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {

                    SuccessResponse response = new SuccessResponse();
                    response.setMessage("OTP verified successfully");
                    return ResponseEntity.created(null).body(response);
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Email not registered");
        }
    }

    public ResponseEntity<?> resetPassword(ResetPasswordDto resetPasswordDto) throws BadRequestException {
        Users user = userRepo.findByEmail(resetPasswordDto.getEmail());
        Doctors doctor = doctorRepo.findByEmail(resetPasswordDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())) {
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                if (otpService.validateOtp(resetPasswordDto.getEmail(), resetPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {
                    user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
                    userRepo.save(user);
                    SuccessResponse response = new SuccessResponse();
                    response.setMessage("Password reset successfully");
                    return ResponseEntity.created(null).body(response);
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else if (doctor != null) {
            if (doctor.getAccountStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(doctor.getAuthType())) {
                    throw new BadRequestException("Please login with " + doctor.getAuthType());
                }
                if (otpService.validateOtp(resetPasswordDto.getEmail(), resetPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {
                    doctor.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
                    doctorRepo.save(doctor);
                    SuccessResponse response = new SuccessResponse();
                    response.setMessage("Password reset successfully");
                    return ResponseEntity.created(null).body(response);
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("Doctor is " + doctor.getAccountStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("Email not registered");
        }
    }

}
