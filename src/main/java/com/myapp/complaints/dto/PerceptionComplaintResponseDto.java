package com.myapp.complaints.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myapp.complaints.enums.ComplaintPriority;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PerceptionComplaintResponseDto(

    ComplaintResponseDto complaintResponseDto,

    String addedByName,
    String addedByIdentifier,

    String complaintNumber,
    ComplaintPriority priority,
    String reason
) {}
