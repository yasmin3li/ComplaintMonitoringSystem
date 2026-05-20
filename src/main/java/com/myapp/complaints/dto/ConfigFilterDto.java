package com.myapp.complaints.dto;

import java.time.LocalDateTime;

public record ConfigFilterDto(

                LocalDateTime end,
                LocalDateTime start,
                long threshold
) {
}
