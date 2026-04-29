package com.myapp.complaints.dto;

import com.myapp.complaints.entity.Governorate;
import com.myapp.complaints.entity.Institution;
import com.myapp.complaints.entity.Sector;

import java.time.LocalDateTime;

public record AssignedToDto(
        String userName,
        String institution,
        String governorate,
        String sector
) {
}
