package com.myapp.complaints.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record ComplaintSolveDto(
        long complaintId,
        String reason,
        @Size(max = 6)
        List<String> images
) {
}
