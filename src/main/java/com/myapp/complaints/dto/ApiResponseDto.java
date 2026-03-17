package com.myapp.complaints.dto;

import java.util.Optional;

public record ApiResponseDto<Object>(
        boolean success,
        String message,
         Optional<Object> data
) {}
