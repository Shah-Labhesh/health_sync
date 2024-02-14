package com.fyp.health_sync.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
//@RequiredArgsConstructor
public class PushNotificationService {


    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }


    public void sendNotification(String title, String body, String token) throws FirebaseMessagingException {

        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .build();
        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        firebaseMessaging.send(message);
    }
}



