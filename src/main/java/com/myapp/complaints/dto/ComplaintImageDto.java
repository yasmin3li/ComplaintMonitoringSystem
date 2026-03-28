package com.myapp.complaints.dto;

import com.myapp.complaints.enums.ImageType;

public record ComplaintImageDto(
        long id,
        String imageUrl,
        ImageType type
) {
}
