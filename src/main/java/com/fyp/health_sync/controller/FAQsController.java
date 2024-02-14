package com.fyp.health_sync.controller;

import java.util.UUID;

import com.fyp.health_sync.exception.InternalServerErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fyp.health_sync.dtos.AddFAQsDto;
import com.fyp.health_sync.dtos.UpdateFAQsDto;
import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.service.FAQsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/faqs")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
public class FAQsController {

    private final FAQsService faqsService;

    @Operation(summary = "Get all FAQs")
    @GetMapping
    public ResponseEntity<?> getAllFAQs() throws BadRequestException, InternalServerErrorException {
        return faqsService.getFAQs();
    }

    @Operation(summary = "Add new FAQ")

    @PostMapping
    public ResponseEntity<?> addFAQs(@Valid @RequestBody AddFAQsDto faqs) throws BadRequestException, InternalServerErrorException {
        return faqsService.addFAQs(faqs);
    }

    @Operation(summary = "Update FAQ")
    @PutMapping("{id}")
    public ResponseEntity<?> updateFAQs(@PathVariable UUID id,  @Valid @RequestBody UpdateFAQsDto faqs) throws BadRequestException, InternalServerErrorException {
        return faqsService.updateFAQs(faqs,id);
    }

    @Operation(summary = "Delete FAQ")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFAQs(@PathVariable UUID id) throws BadRequestException, InternalServerErrorException {
        return faqsService.deleteFAQs(id);
    }
    
}
