package com.myapp.complaints.dto;

import com.myapp.complaints.entity.Governorate;
import com.myapp.complaints.entity.Institution;
import com.myapp.complaints.entity.Sector;

import java.time.LocalDateTime;

public record EmployeeProfileInfoDto(
        String userName,
        boolean emailTemporary,
        String email,
        String phoneNumber,
        String profileImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Institution institution,
        Governorate governorate,
        Sector sector
) {
}
