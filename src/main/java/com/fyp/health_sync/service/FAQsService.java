package com.fyp.health_sync.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fyp.health_sync.exception.InternalServerErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fyp.health_sync.dtos.AddFAQsDto;
import com.fyp.health_sync.dtos.UpdateFAQsDto;
import com.fyp.health_sync.entity.FAQs;
import com.fyp.health_sync.entity.Users;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.repository.FAQsRepo;
import com.fyp.health_sync.repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class FAQsService {

    private final UserRepo userRepo;
    private final FAQsRepo faqsRepo;

    public ResponseEntity<?> getFAQs() throws BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User account is not active");
            }
            return ResponseEntity.ok(faqsRepo.findAllByDeletedAtIsNull());
        }
        catch (BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> addFAQs(AddFAQsDto faqs) throws BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User account is not active");
            }
            if (user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException("User is not authorized to add FAQ");
            }
            FAQs faq = FAQs.builder()
                    .question(faqs.getQuestion())
                    .answer(faqs.getAnswer())
                    .createdAt(LocalDateTime.now())
                    .build();
            faqsRepo.save(faq);
            return ResponseEntity.created (null).body(faqs);
        } catch (BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> deleteFAQs(UUID id) throws BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User account is not active");
            }
            if (user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException("User is not authorized to delete FAQ");
            }

            FAQs faq = faqsRepo.findById(id).orElseThrow(() -> new BadRequestException("FAQ not found"));
            faq.setDeletedAt(LocalDateTime.now());
            faqsRepo.save(faq);
            return ResponseEntity.ok("FAQ deleted successfully");
        }
        catch (BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public ResponseEntity<?> updateFAQs(UpdateFAQsDto faqs, UUID id) throws BadRequestException, InternalServerErrorException {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Users user = userRepo.findByEmail(email);
            if (user == null) {
                throw new BadRequestException("User not found");
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("User account is not active");
            }
            if (user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException("User is not authorized to update FAQ");
            }

            FAQs faq = faqsRepo.findById(id).orElseThrow(() -> new BadRequestException("FAQ not found"));
            if (faqs.getQuestion() != null) {
                faq.setQuestion(faqs.getQuestion());
            }
            if (faqs.getAnswer() != null) {
                faq.setAnswer(faqs.getAnswer());
            }

            faqsRepo.save(faq);
            return ResponseEntity.ok(faq);
        }
        catch (BadRequestException e){
            throw new BadRequestException(e.getMessage());
        }
        catch (Exception e){
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
