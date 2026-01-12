package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public record ComplaintResponseDto(

        Long id,
        String title,
        String description,

        String serviceName,
        String institutionName,

        String status,
        LocalDateTime createdAt,

        LocationDto location
) {}
