package com.fyp.health_sync.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fyp.health_sync.entity.FAQs;

@Repository
public interface FAQsRepo  extends JpaRepository<FAQs, UUID>{
    
    List<FAQs> findAllByDeletedAtIsNull();
}
