package com.myapp.complaints.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateComplaintDto(

        @NotNull
        long complaintId,
        String title,
        String description,
        Long serviceId,
        Long governorateId,
        Long institutionId,
        Long sectorId,
        Double latitude,
        Double longitude,
        String fullAddressText,
        @Size(max = 3)
        List<String> images
) {
}
