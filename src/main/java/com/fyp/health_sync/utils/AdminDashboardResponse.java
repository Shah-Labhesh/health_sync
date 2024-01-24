package com.fyp.health_sync.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardResponse {
    private Integer totalUsers;
    private Integer totalDoctors;
    private Integer totalUnapprovedDoctors;
    private List<DoctorResponse> doctors;
}
