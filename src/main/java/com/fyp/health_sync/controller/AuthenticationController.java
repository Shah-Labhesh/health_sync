package com.fyp.health_sync.controller;


import com.fyp.health_sync.dtos.*;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.service.AuthService;

import com.fyp.health_sync.service.FirebaseAuthenticationService;
import com.fyp.health_sync.utils.SuccessResponse;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;
    private final FirebaseAuthenticationService firebaseAuthenticationService;

    @PostMapping("/google-authenticate")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody String idToken) throws FirebaseAuthException, BadRequestException {
        return firebaseAuthenticationService.authenticate(idToken);
    }

    @Operation(summary = "Register User traditionally")
    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterUserDto userDetails) {
        authService.registerUser(userDetails);
        return ResponseEntity.created(null).body(new SuccessResponse("User registered successfully"));
    }

    @Operation(summary = "Login User & Doctor traditionally")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody @Valid LoginDto loginDto) throws BadRequestException {
        return authService.performLogin(loginDto);
    }


    @Operation(summary = "otp for email verification")
    @PostMapping("/initiate-email-verification/{email}")
    public ResponseEntity<?> initiateEmailVerification(@PathVariable String email) throws BadRequestException, InternalServerErrorException {
        return authService.initiateEmailVerification(email);
    }

    @Operation(summary = "resend otp for email verification")
    @PostMapping("/resend-email-verification/{email}")
    public ResponseEntity<?> resendEmailVerification(@PathVariable String email) throws BadRequestException, InternalServerErrorException {
        return authService.resendEmailVerification(email);
    }

    @Operation(summary = "verify email for all users")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmailVerification(@RequestBody @Valid EmailVerificationDto emailVerification) throws BadRequestException {
        return authService.verifyEmail(emailVerification);
    }

    @Operation(summary = "initiate otp for password reset")
    @PostMapping("/initiate-password-reset/{email}")
    public ResponseEntity<?> initiatePasswordReset(@PathVariable String email) throws BadRequestException, InternalServerErrorException {
        return authService.initiateForgotPassword(email);
    }

    @Operation(summary = "resend otp for password reset")
    @PostMapping("/resend-password-reset/{email}")
    public ResponseEntity<?> resendPasswordReset(@PathVariable String email) throws BadRequestException, InternalServerErrorException {
        return authService.resendForgotPassword(email);
    }

    @Operation(summary = "verify otp for password reset")
    @PostMapping("/verify-password-reset")
    public ResponseEntity<?> verifyPasswordReset(@RequestBody @Valid VerifyForgotPasswordDto verifyPassword) throws BadRequestException {
        return authService.verifyForgotPassword(verifyPassword);
    }

    @Operation(summary = "reset password for user and doctor")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDto resetPassword) throws BadRequestException {
        return authService.resetPassword(resetPassword);
    }

}
