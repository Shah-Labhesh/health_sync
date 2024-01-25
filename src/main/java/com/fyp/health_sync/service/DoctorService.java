package com.fyp.health_sync.service;

import com.fyp.health_sync.repository.SpecialityRepo;
import com.fyp.health_sync.repository.UserRepo;
import com.fyp.health_sync.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {

   private final UserRepo userRepo;
}
