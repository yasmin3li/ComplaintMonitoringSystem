package com.myapp.complaints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

        // بيانات الموقع من Leaflet
        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        String fullAddressText,

        @Size(max = 3)
        List<String> images
//        ,List<String> imageDescriptions
) {}

