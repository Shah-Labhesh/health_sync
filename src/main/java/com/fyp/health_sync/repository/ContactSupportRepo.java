package com.fyp.health_sync.repository;

import com.fyp.health_sync.entity.ContactSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactSupportRepo extends JpaRepository<ContactSupport, UUID> {

    Integer countAllByResponseMessageNotNullAndUserIsNotNull();

    Integer countAllByResponseMessageIsNullAndUserIsNotNull();

}
