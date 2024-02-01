package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.ContactSupport;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.repository.ContactSupportRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ContactSupportResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactSupportService {

    private final ContactSupportRepo contactSupportRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> contactSupport(String email, String message) {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
       Users user = userRepo.findByEmail(authentication);
         if (user == null){
              return ResponseEntity.badRequest().body("User not found");
         }
        contactSupportRepo.save(ContactSupport.builder()
                .email(email)
                .message(message)
                .user(user)
                .build());
        return ResponseEntity.ok(new SuccessResponse("Message sent successfully"));
    }


    public ResponseEntity<?> getAllMessages() {
        List<ContactSupport> contactSupports = contactSupportRepo.findAll();
        List<ContactSupportResponse> contactSupportResponses = new ArrayList<>();
        for (ContactSupport contactSupport : contactSupports) {
            contactSupportResponses.add(new ContactSupportResponse().castToResponse(contactSupport));
        }
        return ResponseEntity.ok(contactSupportResponses);
    }

    public ResponseEntity<?> responseMessage(String responseMessage, UUID id) {
        ContactSupport contactSupport = contactSupportRepo.findById(id).orElseThrow( () -> new RuntimeException("Message not found"));
        if (contactSupport == null){
            return ResponseEntity.badRequest().body("Message not found");
        }
        contactSupport.setResponseMessage(responseMessage);
        contactSupportRepo.save(contactSupport);
        return ResponseEntity.ok(new SuccessResponse("Response sent successfully"));
    }

}
