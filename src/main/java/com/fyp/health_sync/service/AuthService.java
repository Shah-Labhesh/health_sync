package com.fyp.health_sync.service;

import com.fyp.health_sync.config.CustomUserDetails;
import com.fyp.health_sync.config.JwtHelper;
import com.fyp.health_sync.dtos.*;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.OtpType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.LoginResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
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
    private final JwtHelper jwtHelper;
    private final MailService mailService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepo.findByEmail(username);
        if (user != null) {
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UsernameNotFoundException("User not found.");
            }
            return new CustomUserDetails(user.getEmail(), user.getPassword(), user.getRoles());
        }
        else {
            throw new UsernameNotFoundException("User not found.");
        }

    }

    private Boolean validateAuthType(AuthType authType) {
        return authType == AuthType.Google || authType == AuthType.Twitter;
    }

    public void registerUser(RegisterUserDto userDetails) {
        Users user = userRepo.findByEmail(userDetails.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.DELETED) {
                throw new DataIntegrityViolationException("Email is in trash. Please contact admin");
            } else {
                throw new DataIntegrityViolationException("User already exists");
            }
        }

        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        userDetails.setPassword(bCrypt.encode(userDetails.getPassword()));
        Users newUser = Users.builder()
                .id(UUID.randomUUID())
                .name(userDetails.getName())
                .email(userDetails.getEmail())
                .password(userDetails.getPassword())
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .status(UserStatus.ACTIVE)
                .role(userDetails.getRole())
                .authType(AuthType.Traditional)
                .build();
        userRepo.save(newUser);
    }

    public ResponseEntity<?> performLogin(LoginDto credentials) throws BadRequestException {
        Users user = userRepo.findByEmail(credentials.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())) {
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }else{
                    return loginUser(credentials);
                }

            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }  else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    private ResponseEntity<?> loginUser(LoginDto userDetails) throws BadRequestException {
        Users user = userRepo.findByEmail(userDetails.getEmail());
        if (passwordEncoder.matches(userDetails.getPassword(), user.getPassword())) {
                if(user.getIsVerified()){
            UserDetails loggedUser = loadUserByUsername(user.getEmail());
            String token = jwtHelper.generateToken(loggedUser);
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setVerified(user.getIsVerified());
            response.setAuthType(user.getAuthType());
            response.setStatus(user.getStatus());
            response.setRole(user.getRole().name());
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

    public ResponseEntity<?> performGoogleAuth(String name, String email) throws BadRequestException {
        Users user = userRepo.findByEmail(email);
        if (user != null){
            if (user.getStatus() == UserStatus.ACTIVE){
               if (user.getAuthType() == AuthType.Google){
                  return googleLogin(email);
               }else{
                   throw new BadRequestException("Please login by " + user.getAuthType());
               }
            }else{
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }else{
            return registerGoogleUser(name, email);
        }
    }

    private ResponseEntity<?> googleLogin(String email) {
        Users user = userRepo.findByEmail(email);
        String token = jwtHelper.generateToken(loadUserByUsername(user.getEmail()));
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setVerified(user.getIsVerified());
        response.setAuthType(user.getAuthType());
        response.setStatus(user.getStatus());
        response.setRole(user.getRole().name());
        response.setMessage("User Login successfully");
        return ResponseEntity.created(null).body(response);
    }

    private ResponseEntity<?> registerGoogleUser(String name, String email) {
        Users newUser = Users.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .createdAt(LocalDateTime.now())
                .isVerified(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .authType(AuthType.Google)
                .build();
        userRepo.save(newUser);
        return googleLogin(email);
    }

    public ResponseEntity<?> initiateEmailVerification(String email) throws BadRequestException, InternalServerErrorException {

        Users user = userRepo.findByEmail(email);
        if (user != null) {
            if (user.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }

            if (user.getStatus() == UserStatus.ACTIVE) {
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Email Verification", "verify your email");
                return ResponseEntity.created(null).body(new SuccessResponse("OTP sent successfully"));
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }
        else {
            throw new BadRequestException("User not found");
        }
    }

    public ResponseEntity<?> resendEmailVerification(String email) throws BadRequestException, InternalServerErrorException {
        Users user = userRepo.findByEmail(email);

        if (user != null) {
            if (user.getIsVerified()) {
                throw new BadRequestException("Email already verified");
            }
            if (user.getStatus() != UserStatus.DELETED) {
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.EMAIL_VERIFICATION), "Resend Email Verification", "verify your email");
                return ResponseEntity.created(null).body(new SuccessResponse("OTP re-sent successfully"));
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        } else {
            throw new BadRequestException("User not found");
        }
    }


    public ResponseEntity<?> verifyEmail(EmailVerificationDto emailVerificationDto) throws BadRequestException {
        Users user = userRepo.findByEmail(emailVerificationDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (user.getIsVerified()) {
                    throw new BadRequestException("Email already verified");
                } else {
                    if (otpService.validateOtp(emailVerificationDto.getEmail(), emailVerificationDto.getOtp(), OtpType.EMAIL_VERIFICATION)) {
                        user.setIsVerified(true);
                        userRepo.save(user);
                        return ResponseEntity.created(null).body(new SuccessResponse("Email verified successfully"));
                    } else {
                        throw new BadRequestException("Invalid OTP");
                    }
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }
        else {
            throw new BadRequestException("Email not registered");
        }
    }

    public ResponseEntity<?> initiateForgotPassword(String email) throws BadRequestException, InternalServerErrorException {
        Users user = userRepo.findByEmail(email);
        if (user != null) {

            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Password Reset", "reset your password");

                return ResponseEntity.created(null).body(new SuccessResponse("OTP sent successfully"));
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    public ResponseEntity<?> resendForgotPassword(String email) throws BadRequestException, InternalServerErrorException {
        Users user = userRepo.findByEmail(email);
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                mailService.sendEmail(user.getName(), email, otpService.getOtp(email, OtpType.PASSWORD_RESET), "Resend Password Reset", "reset your password");
                return ResponseEntity.created(null).body(new SuccessResponse("OTP re-sent successfully"));
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }

        else {
            throw new BadRequestException("Invalid Credentials");
        }
    }

    // verifyForgotPassword
    public ResponseEntity<?> verifyForgotPassword(VerifyForgotPasswordDto verifyForgotPasswordDto) throws BadRequestException {
        Users user = userRepo.findByEmail(verifyForgotPasswordDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())){
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                if (otpService.validateOtp(verifyForgotPasswordDto.getEmail(), verifyForgotPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {


                    return ResponseEntity.created(null).body(new SuccessResponse("OTP verified successfully"));
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }
        else {
            throw new BadRequestException("Email not registered");
        }
    }

    public ResponseEntity<?> resetPassword(ResetPasswordDto resetPasswordDto) throws BadRequestException {
        Users user = userRepo.findByEmail(resetPasswordDto.getEmail());
        if (user != null) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                if (validateAuthType(user.getAuthType())) {
                    throw new BadRequestException("Please login with " + user.getAuthType());
                }
                if (otpService.validateOtp(resetPasswordDto.getEmail(), resetPasswordDto.getOtp(), OtpType.PASSWORD_RESET)) {
                    user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
                    userRepo.save(user);
                    return ResponseEntity.created(null).body(new SuccessResponse("Password reset successfully"));
                } else {
                    throw new BadRequestException("Invalid OTP");
                }
            } else {
                throw new BadRequestException("User is " + user.getStatus() + " Please contact admin");
            }
        }
        else {
            throw new BadRequestException("Email not registered");
        }
    }

}
