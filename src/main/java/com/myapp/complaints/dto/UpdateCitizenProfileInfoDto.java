package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public record UpdateCitizenProfileInfoDto(
        String userName,
        String profileImageUrl,
        String birthDate
) {}
