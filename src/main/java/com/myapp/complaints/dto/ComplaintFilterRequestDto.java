package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintState;

public record ComplaintFilterRequestDto(
        Long employeeId,
        Long governorateId,
        Long sectorId,
        Long institutionId,
        String state,
//        Boolean myComplaints,
        Integer page,
        Integer size,
        String keyword
) {}
