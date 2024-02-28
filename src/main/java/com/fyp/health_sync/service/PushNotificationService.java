package com.fyp.health_sync.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public void sendNotification(String title, String body, String token) throws FirebaseMessagingException {

        try {
            Message message = Message
                    .builder()
                    .setToken(token)
                    .setNotification(Notification
                            .builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
