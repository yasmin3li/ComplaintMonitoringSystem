package com.myapp.complaints.dto;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public record ManagerComplaintResponseDto(

    ReceptionComplaintResponseDto complaintDetails,

    Long assignedToId,
    String assignedTo,
    String email

) {}
