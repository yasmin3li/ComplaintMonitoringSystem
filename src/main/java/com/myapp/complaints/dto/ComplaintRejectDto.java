package com.myapp.complaints.dto;

public record ComplaintRejectDto(
        long complaintId,
        String reason
) {
}
