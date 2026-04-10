package com.myapp.complaints.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintResponseDto(

        Long id,
        String title,
        String description,

        String serviceName,
        String institutionName,

        String status,
        LocalDateTime createdAt,

        LocationDto location,

        List<ComplaintImageDto> complaintImageDtoList
) {}
