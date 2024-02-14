package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.ContactSupport;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.InternalServerErrorException;
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

    public ResponseEntity<?> contactSupport(String email, String message) throws BadRequestException, InternalServerErrorException {
       try{
           String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
           Users user = userRepo.findByEmail(authentication);
           if (user == null){
               throw new BadRequestException("User not found");
           }
           contactSupportRepo.save(ContactSupport.builder()
                   .email(email)
                   .message(message)
                   .user(user)
                   .build());
           return ResponseEntity.created(null) .body(new SuccessResponse("Message sent successfully"));
       }
       catch (BadRequestException e) {
           throw new BadRequestException(e.getMessage());
       }
         catch (Exception e){
              throw new InternalServerErrorException(e.getMessage());
         }
    }


    public ResponseEntity<?> getAllMessages() throws InternalServerErrorException {
        try {
            List<ContactSupport> contactSupports = contactSupportRepo.findAll();
            List<ContactSupportResponse> contactSupportResponses = new ArrayList<>();
            for (ContactSupport contactSupport : contactSupports) {
                contactSupportResponses.add(new ContactSupportResponse().castToResponse(contactSupport));
            }
            return ResponseEntity.ok(contactSupportResponses);
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> responseMessage(String responseMessage, UUID id) throws InternalServerErrorException {
        try {
            ContactSupport contactSupport = contactSupportRepo.findById(id).orElseThrow( () -> new RuntimeException("Message not found"));
            if (contactSupport == null){
                return ResponseEntity.badRequest().body("Message not found");
            }
            contactSupport.setResponseMessage(responseMessage);
            contactSupportRepo.save(contactSupport);
            return ResponseEntity.ok(new SuccessResponse("Response sent successfully"));
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }

}
