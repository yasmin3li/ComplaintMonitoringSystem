package com.myapp.complaints.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record VerifyUserDto (
    @Email(message = "Invalid email format")
    @NotEmpty
    String email,
    @NotEmpty
    String code
){}
