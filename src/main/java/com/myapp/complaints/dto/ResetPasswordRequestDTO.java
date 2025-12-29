package com.myapp.complaints.dto;

import jakarta.validation.constraints.NotEmpty;

public record ResetPasswordRequestDTO(
        @NotEmpty
         String emailOrPhone,
        @NotEmpty
        String token,
        @NotEmpty
        String newPassword
) {}
