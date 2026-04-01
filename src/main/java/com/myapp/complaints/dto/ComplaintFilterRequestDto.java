package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ComplaintState;

public record ComplaintFilterRequestDto(
        Long governorateId,
        Long sectorId,
        Long institutionId,
        String state,
//        Boolean myComplaints,
        int page,
        int size,
        String keyword
) {}
