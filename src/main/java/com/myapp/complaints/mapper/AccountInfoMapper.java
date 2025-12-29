package com.myapp.complaints.mapper;


import com.myapp.complaints.dto.CitizenRegistrationDto;
import com.myapp.complaints.dto.EmployeeRegistrationDto;
import com.myapp.complaints.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountInfoMapper {

    private final PasswordEncoder passwordEncoder;

    public Account fromCitizenDto(CitizenRegistrationDto dto) {
        Account account = new Account();
        account.setUserName(dto.userName());
        account.setPhoneNumber(dto.phoneNumber());
        account.setNationalNumber(dto.nationalNumber());
        account.setPassword(validateAndEncodePassword(dto.password()));

        if (dto.email() == null || dto.email().isBlank()) {
            String phone = dto.phoneNumber() != null ? dto.phoneNumber() : "user" + System.currentTimeMillis();
            account.setEmail(phone + "@example.com");
            account.setEmailTemporary(true);
        } else {
            account.setEmail(dto.email());
            account.setEmailTemporary(false);
        }

        return account;
    }

    public Account fromEmployeeDto(EmployeeRegistrationDto dto) {
        Account account = new Account();
        account.setUserName(dto.userName());
        account.setPhoneNumber(dto.phoneNumber());
        account.setNationalNumber(dto.nationalNumber());
        account.setPassword(validateAndEncodePassword(dto.password()));

        if (dto.email() == null || dto.email().isBlank()) {
            String phone = dto.phoneNumber() != null ? dto.phoneNumber() : "user" + System.currentTimeMillis();
            account.setEmail(phone + "@example.com");
            account.setEmailTemporary(true);
        } else {
            account.setEmail(dto.email());
            account.setEmailTemporary(false);
        }

        return account;
    }

    private  String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
//        if (!rawPassword.matches(".*[a-z].*")) {
//            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
//        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
//        if (!rawPassword.matches(".*[]\\[!@#$%^&*()_+\\-={};':\"\\\\|,.<>/?].*")) {
//            throw new IllegalArgumentException("Password must contain at least one special character");
//        }
        // تشفير كلمة المرور
        return passwordEncoder.encode(rawPassword);
    }


}
