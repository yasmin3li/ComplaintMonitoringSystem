package com.myapp.complaints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ComplaintCreateDto(

        @NotEmpty

        String title,

        @NotEmpty
        String description,

        @NotNull
        Long serviceId,

        @NotNull
        Long institutionId,

        @NotNull
        Long governorateId,

        @NotNull
        Long sectorId,

        @NotNull
        Long addressId
) {}

