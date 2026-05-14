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
        LocalDateTime updatedAt,

        LocationDto location,

        String reason,
        boolean owner,
        List<ComplaintImageDto> complaintImageDtoList
) {}
