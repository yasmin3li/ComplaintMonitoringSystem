package com.myapp.complaints.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConfigFilterDto(

                LocalDate end,
                LocalDate start) {
}
