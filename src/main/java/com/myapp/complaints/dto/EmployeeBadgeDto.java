package com.myapp.complaints.dto;

import com.myapp.complaints.enums.BadgeLevel;
import com.myapp.complaints.enums.BadgeType;

public record EmployeeBadgeDto(

        BadgeType type,
        String title,
        String description,
        BadgeLevel level,
        String icon
) {
}
