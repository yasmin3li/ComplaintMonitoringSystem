package com.myapp.complaints.dto;

public record UpdateEmployeeProfileInfoDto(
        String userName,
        String email,
        String phoneNumber,
        String profileImageUrl
) {
}
