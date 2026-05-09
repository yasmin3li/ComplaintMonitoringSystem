package com.myapp.complaints.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReceptionComplaintResponseDto(

    ComplaintResponseDto complaintResponseDto,

    String addedByName,
    String addedByIdentifier,

    String complaintNumber,
    String priority,
    String reason
) {}
