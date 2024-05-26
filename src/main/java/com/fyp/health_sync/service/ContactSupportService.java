package com.fyp.health_sync.service;

import com.fyp.health_sync.entity.ContactSupport;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.repository.ContactSupportRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ContactSupportResponse;
import com.fyp.health_sync.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContactSupportService {

    private final ContactSupportRepo contactSupportRepo;
    private final UserRepo userRepo;

    public ResponseEntity<?> contactSupport(String email, String message)
            throws BadRequestException, InternalServerErrorException {
        try {
            String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(authentication);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            contactSupportRepo.save(ContactSupport.builder()
                    .email(email)
                    .message(message)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build());
            return ResponseEntity.created(null).body(new SuccessResponse("Message sent successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> getAllMessages()
            throws InternalServerErrorException, BadRequestException, ForbiddenException {

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.ADMIN) {
                throw new ForbiddenException("You are not authorized to view messages");
            }
            List<ContactSupport> contactSupports = contactSupportRepo.findAll();
            List<ContactSupportResponse> contactSupportResponses = new ArrayList<>();
            for (ContactSupport contactSupport : contactSupports) {
                contactSupportResponses.add(new ContactSupportResponse().castToResponse(contactSupport));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("totalMessage", contactSupportResponses.size() );
            response.put("pendingResponse", contactSupportRepo.countAllByResponseMessageIsNullAndUserIsNotNull() );
            response.put("totalResponse", contactSupportRepo.countAllByResponseMessageNotNullAndUserIsNotNull() );
            response.put("contactSupport", contactSupportResponses);
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> responseMessage(String responseMessage, UUID id)
            throws InternalServerErrorException, BadRequestException, ForbiddenException {
        try {
            if (responseMessage == null || responseMessage.isEmpty()) {
                throw new BadRequestException("Response message is required");
            }
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getRole() != UserRole.ADMIN) {
                throw new ForbiddenException("User is not authorized to send response");
            }
            ContactSupport contactSupport = contactSupportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Message not found"));
            if (contactSupport == null) {
                throw new BadRequestException("Message not found");
            }
            contactSupport.setResponseMessage(responseMessage);
            contactSupportRepo.save(contactSupport);
            return ResponseEntity.created(null).body(new SuccessResponse("Response sent successfully"));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ForbiddenException e) {
            throw new ForbiddenException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

}
