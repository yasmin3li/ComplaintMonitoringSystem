package com.myapp.complaints.dto;

import java.util.Optional;

public record ApiResponseDto<T>(
        boolean success,
        String message,
         T data
) {}
