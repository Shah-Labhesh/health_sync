package com.fyp.health_sync.service;

import com.fyp.health_sync.exception.BadRequestException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseAuthenticationService {

    private final FirebaseAuth firebaseAuth;
    private final AuthService authService;

    public ResponseEntity<?> authenticate(String name, String email) throws  BadRequestException {
        try {
//            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
//            System.out.println(decodedToken.getEmail());
//            System.out.println(decodedToken.getName());
            return authService.performGoogleAuth(name, email);

        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }


    }
}
