package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public record CitizenProfileInfoDto(
        String userName,
        String email,
        String phoneNumber,
        boolean emailTemporary,
        String profileImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String birthDate
        )
{}
