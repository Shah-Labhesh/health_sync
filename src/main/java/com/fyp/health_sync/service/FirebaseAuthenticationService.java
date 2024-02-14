package com.fyp.health_sync.service;

import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseAuthenticationService {

    private final AuthService authService;

    public ResponseEntity<?> authenticate(String name, String email) throws BadRequestException, InternalServerErrorException {
        try {
            return authService.performGoogleAuth(name, email);

        }
        catch (BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }


    }
}
