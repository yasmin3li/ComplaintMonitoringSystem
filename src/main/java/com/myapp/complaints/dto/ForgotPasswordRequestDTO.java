package com.myapp.complaints.dto;

import jakarta.validation.constraints.NotEmpty;

public record ForgotPasswordRequestDTO(
        @NotEmpty(message = "this field shouldn't be empty or zero length")
        String emailOrPhone
) {}
