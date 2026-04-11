package com.myapp.complaints.dto;

import java.util.List;

public record PerceptionComplaintResponseDto(

    ComplaintResponseDto complaintResponseDto,

    String addedByName,
    String addedByIdentifier,

    String complaintNumber

) {}
