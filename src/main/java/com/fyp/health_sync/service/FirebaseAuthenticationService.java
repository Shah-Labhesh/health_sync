package com.fyp.health_sync.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseAuthenticationService {

    private final FirebaseAuth firebaseAuth;



    public String authenticate(String token) throws FirebaseAuthException {
        FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
        System.out.println(decodedToken.getEmail());
        System.out.println(decodedToken.getName());

        return decodedToken.getUid();
    }
}
