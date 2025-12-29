package com.myapp.complaints.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CitizenRegistrationDto(

        @NotEmpty(message = "nationalNumber  must not be empty")
        String nationalNumber,

        String birthDate,

        @NotEmpty(message = "User Name must not be empty")
                String userName,
        @NotEmpty(message = "User phoneNumber must not be empty") //Neither null nor 0 size
        String phoneNumber,

        @Email(message = "Invalid email format")//these annotations are used with @valid and binding result with dto only
        String email,
        String image,

        @NotEmpty(message = "User password must not be empty")
        String password
) {}