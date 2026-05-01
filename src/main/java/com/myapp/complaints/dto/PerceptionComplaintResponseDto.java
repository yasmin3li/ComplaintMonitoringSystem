package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintPriority;

import java.util.List;

public record PerceptionComplaintResponseDto(

    ComplaintResponseDto complaintResponseDto,

    String addedByName,
    String addedByIdentifier,

    String complaintNumber,
    ComplaintPriority priority
) {}
