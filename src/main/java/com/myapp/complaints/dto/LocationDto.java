package com.myapp.complaints.dto;

public record LocationDto(
        Long governorateId,
        String governorateName,

        Long sectorId,
        String sectorName,

        AddressDto address
) {}

